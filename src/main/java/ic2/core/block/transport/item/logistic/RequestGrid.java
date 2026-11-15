package ic2.core.block.transport.item.logistic;

import ic2.api.tiles.tubes.ILimiterTube;
import ic2.api.tiles.tubes.IProviderTube;
import ic2.api.tiles.tubes.IRequestTube;
import ic2.api.tiles.tubes.ITube;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.LongAverager;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.MathUtils;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.ObjIntConsumer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RequestGrid {
   static final int DIRTY = 1;
   static final int SPLIT = 2;
   static final int RESET = 4;
   static final int POTENTIAL_SPLIT = 8;
   static final BitSet USED_GRIDS = new BitSet();
   TubeGrid grid;
   int gridId;
   int flags;
   Map<ITube, RequestGrid.TubeConnection> tubes = CollectionUtils.<ITube, RequestGrid.TubeConnection>createLinkedMap();
   Set<RequestGrid.TubeConnection> looseEnds = CollectionUtils.<RequestGrid.TubeConnection>createLinkedSet();
   List<IRequestTube> requesters = CollectionUtils.<IRequestTube>createList();
   List<ILimiterTube> limiters = CollectionUtils.<ILimiterTube>createList();
   Set<IProviderTube> providers = CollectionUtils.<IProviderTube>createLinkedSet();
   List<RequestGrid.TubeRequest> allRequests = CollectionUtils.<RequestGrid.TubeRequest>createList();
   Long2ObjectMap<List<TubeGrid.TubePath>> limiterPaths = new Long2ObjectLinkedOpenHashMap();
   Map<IRequestTube, Set<RequestGrid.TubeRequest>> tubeRequests = CollectionUtils.<IRequestTube, Set<RequestGrid.TubeRequest>>createLinkedMap();
   RequestGrid.TubeRequester requester = new RequestGrid.TubeRequester();
   LongAverager totalLag = new LongAverager(200);

   public RequestGrid(TubeGrid grid) {
      this.grid = grid;
      this.gridId = USED_GRIDS.nextClearBit(1);
      USED_GRIDS.set(this.gridId);
   }

   void setFlag(int flag) {
      this.flags |= flag;
   }

   void clearFlag(int flag) {
      this.flags &= ~flag;
   }

   boolean hasFlag(int flag) {
      return (this.flags & flag) != 0;
   }

   public int getId() {
      return this.gridId;
   }

   public void destroy() {
      USED_GRIDS.clear(this.gridId);

      for (IRequestTube tube : this.requesters) {
         tube.onRequestsReset();
      }

      this.allRequests.clear();
      this.tubeRequests.clear();
      this.limiterPaths.clear();
   }

   public void markSplit() {
      this.markDirty(false);
      this.setFlag(8);
   }

   public void markDirty(boolean force) {
      if (!this.hasFlag(1) && !this.hasFlag(2) || force) {
         this.setFlag(1);
         this.grid.dirtyGrids.add(this.gridId);
      }
   }

   public void tick() {
      if (!this.requesters.isEmpty() && !this.providers.isEmpty()) {
         long lag = System.nanoTime();
         long time = this.grid.world.getGameTime();
         if (time % 20L == 0L) {
            Collections.shuffle(this.requesters);

            for (IRequestTube tube : this.requesters) {
               tube.provideRequests(this.requester.setCurrentTube(tube));
            }

            this.requester.reset();
         }

         if (time % 10L == 0L) {
            if (this.allRequests.isEmpty()) {
               return;
            }

            Collections.shuffle(this.allRequests);
            if (this.limiters.isEmpty()) {
               this.allRequests.removeIf(this::onRequestFinished);
            } else {
               this.allRequests.removeIf(this::onLimitedRequestFinished);
            }
         }

         this.totalLag.addEntry(System.nanoTime() - lag);
      }
   }

   public void clearRequests(IRequestTube tube) {
      Set<RequestGrid.TubeRequest> requests = this.tubeRequests.remove(tube);
      if (requests != null) {
         this.allRequests.removeIf(requests::contains);
      }
   }

   public Object2IntMap<Item> getPotentialItems() {
      Object2IntLinkedOpenHashMap<Item> items = new Object2IntLinkedOpenHashMap();

      for (IProviderTube tube : this.providers) {
         tube.getItemsProvided((K, V) -> items.addTo(K.getItem(), V));
      }

      return items;
   }

   public void onRequestLost(UUID requestId, ItemStack stack) {
      for (IRequestTube tube : this.requesters) {
         if (tube.getRequestId().equals(requestId)) {
            tube.onRequestLost(StackUtil.copyWithSize(stack, 1), stack.getCount());
            break;
         }
      }
   }

   private boolean onLimitedRequestFinished(RequestGrid.TubeRequest request) {
      for (TubeGrid.TubePath path : this.limiterPaths.getOrDefault(request.tube.getPosition().asLong(), Collections.emptyList())) {
         if ((path.validColors == -1 || request.color != null && (path.validColors & 1 << request.color.getId()) != 0) && request.request(path.provider)) {
            break;
         }
      }

      request.onFinished();
      if (request.isEmpty()) {
         request.remove(this.tubeRequests);
         return true;
      } else {
         return false;
      }
   }

   private boolean onRequestFinished(RequestGrid.TubeRequest request) {
      for (IProviderTube tube : this.providers) {
         if (request.request(tube)) {
            break;
         }
      }

      request.onFinished();
      if (request.isEmpty()) {
         request.remove(this.tubeRequests);
         return true;
      } else {
         return false;
      }
   }

   private void addRequest(RequestGrid.TubeRequest request) {
      this.allRequests.add(request);
      Set<RequestGrid.TubeRequest> mapped = this.tubeRequests.computeIfAbsent(request.tube, k -> CollectionUtils.createLinkedSet());
      mapped.add(request);
   }

   public void addTube(ITube tube) {
      if (!this.tubes.containsKey(tube)) {
         this.grid.tubeToGrid.put(tube.getPosition().asLong(), this.gridId);
         RequestGrid.TubeConnection connection = new RequestGrid.TubeConnection(tube, this);
         this.tubes.put(tube, connection);
         if (tube instanceof IRequestTube request && !(tube instanceof IProviderTube) && !(tube instanceof ILimiterTube)) {
            this.requesters.add(request);
         }

         if (tube instanceof IProviderTube provider && !(tube instanceof IRequestTube) && !(tube instanceof ILimiterTube)) {
            this.providers.add(provider);
         }

         if (tube instanceof ILimiterTube limiter && !(tube instanceof IProviderTube) && !(tube instanceof IRequestTube)) {
            this.limiters.add(limiter);
         }

         if (connection.links.size() > 1 && this.flags == 0) {
            this.markDirty(false);
         }
      }
   }

   public void addGrid(RequestGrid grid) {
      for (ITube tube : grid.tubes.keySet()) {
         this.tubes.put(tube, new RequestGrid.TubeConnection(tube, this));
      }

      this.requesters.addAll(grid.requesters);
      this.providers.addAll(grid.providers);
      this.limiters.addAll(grid.limiters);
   }

   public void finishMerge() {
      for (ITube pipe : this.tubes.keySet()) {
         this.grid.tubeToGrid.put(pipe.getPosition().asLong(), this.gridId);
      }

      this.markDirty(true);
   }

   public void removeTube(ITube tube) {
      RequestGrid.TubeConnection connection = this.tubes.remove(tube);
      if (connection != null) {
         this.looseEnds.remove(connection);
         this.looseEnds.addAll(connection.links);
         connection.destroy();
         if (tube instanceof IRequestTube) {
            this.requesters.remove(tube);
            Set<RequestGrid.TubeRequest> requests = this.tubeRequests.remove(tube);
            if (requests != null) {
               this.allRequests.removeAll(requests);
            }
         }

         if (tube instanceof IProviderTube) {
            this.providers.remove(tube);
         }

         if (tube instanceof ILimiterTube) {
            this.limiters.remove(tube);
         }
      }
   }

   public void processChanges() {
      if (this.hasFlag(8) && this.isGridSplit()) {
         this.grid.splitGrid(this);
      } else {
         this.flags = 0;
         this.limiterPaths.clear();
         if (!this.limiters.isEmpty()) {
            for (IRequestTube tube : this.requesters) {
               this.limiterPaths.put(tube.getPosition().asLong(), this.grid.discoverPaths(tube, new ObjectLinkedOpenHashSet(this.providers)));
            }
         }
      }
   }

   public void finishSplit() {
      this.clearFlag(2);
      this.markDirty(true);

      for (ITube pipe : this.tubes.keySet()) {
         this.grid.tubeToGrid.put(pipe.getPosition().asLong(), this.gridId);
      }
   }

   boolean isGridSplit() {
      if (this.looseEnds.isEmpty()) {
         return false;
      } else {
         PriorityQueue<RequestGrid.TubeConnection> todoList = new ObjectArrayFIFOQueue();
         todoList.enqueue(this.looseEnds.iterator().next());
         Set<RequestGrid.TubeConnection> foundConnections = CollectionUtils.<RequestGrid.TubeConnection>createSet();

         while (!todoList.isEmpty() && !this.looseEnds.isEmpty()) {
            foundConnections.add((RequestGrid.TubeConnection)todoList.first());
            this.looseEnds.remove(todoList.first());

            for (RequestGrid.TubeConnection connection : ((RequestGrid.TubeConnection)todoList.dequeue()).links) {
               if (foundConnections.add(connection)) {
                  todoList.enqueue(connection);
                  this.looseEnds.remove(connection);
               }
            }
         }

         return !this.looseEnds.isEmpty() && foundConnections.size() != this.tubes.size();
      }
   }

   static class RequestDetector {
      int totalCount;
      Long2IntLinkedOpenHashMap specificCounts = new Long2IntLinkedOpenHashMap();

      public RequestDetector(Item ignored) {
      }

      public void add(int count, long position) {
         this.totalCount += count;
         this.specificCounts.addTo(position, count);
      }

      public void markAmount(int count, long position) {
         this.totalCount -= count;
         this.specificCounts.merge(position, count, MathUtils.REMOVAL);
      }

      public int getPresentCount(long position) {
         return this.totalCount - this.specificCounts.get(position);
      }
   }

   static class TubeConnection {
      Set<RequestGrid.TubeConnection> links = CollectionUtils.<RequestGrid.TubeConnection>createLinkedSet();

      public TubeConnection(ITube tube, RequestGrid grid) {
         for (TubeGrid.TubeLink link : ((TubeGrid.Connection)grid.grid.connections.get(tube)).getLinks()) {
            RequestGrid.TubeConnection other = grid.tubes.get(link.target);
            if (other != null) {
               other.links.add(this);
               this.links.add(other);
            }
         }
      }

      public void destroy() {
         for (RequestGrid.TubeConnection connection : this.links) {
            connection.links.remove(this);
         }

         this.links.clear();
      }
   }

   static class TubeRequest {
      IRequestTube tube;
      long requestPos;
      ItemStack request;
      DyeColor color;
      int amount;
      UUID requestId;
      int delivered;

      public TubeRequest(IRequestTube tube, ItemStack request, DyeColor color, int amount, UUID requestId) {
         this.tube = tube;
         this.requestPos = tube.getRequestSource();
         this.request = request;
         this.color = color;
         this.amount = amount;
         this.requestId = requestId;
      }

      public void remove(Map<IRequestTube, Set<RequestGrid.TubeRequest>> tubeRequest) {
         Set<RequestGrid.TubeRequest> requests = tubeRequest.get(this.tube);
         if (requests != null) {
            requests.remove(this);
            if (requests.isEmpty()) {
               tubeRequest.remove(this.tube);
            }
         }
      }

      public boolean request(IProviderTube provider) {
         if (this.requestPos == provider.getProviderSource()) {
            return false;
         } else {
            this.delivered = this.delivered + provider.provideItems(this.request, this.amount - this.delivered, this.color, this.requestId);
            return this.delivered > 0;
         }
      }

      public boolean isEmpty() {
         return this.delivered >= this.amount || this.amount <= 0;
      }

      public void onFinished() {
         if (this.delivered != 0) {
            this.amount = this.amount - this.delivered;
            this.tube.onRequestFulfilled(this.request, this.delivered);
            this.delivered = 0;
         }
      }
   }

   class TubeRequester implements IRequestTube.ITubeRequester, ObjIntConsumer<ItemStack> {
      IRequestTube currentTube;
      long currentProvider;
      Map<Item, RequestGrid.RequestDetector> itemsInNetwork = CollectionUtils.<Item, RequestGrid.RequestDetector>createLinkedMap();
      Map<DyeColor, Map<Item, RequestGrid.RequestDetector>> limitedItemsInNetwork = CollectionUtils.<DyeColor, Map<Item, RequestGrid.RequestDetector>>createLinkedMap();
      DyeColor currentDyeColor = null;
      boolean isLimited = false;
      boolean tested = false;

      public void reset() {
         this.limitedItemsInNetwork.clear();
         this.itemsInNetwork.clear();
         this.tested = false;
         this.isLimited = false;
         this.currentProvider = 0L;
         this.currentDyeColor = null;
      }

      public IRequestTube.ITubeRequester setCurrentTube(IRequestTube currentTube) {
         this.currentTube = currentTube;
         this.isLimited = !RequestGrid.this.limiters.isEmpty();
         return this;
      }

      @Override
      public void requestItems(ItemStack stack, int amount, DyeColor color, UUID requestId) {
         RequestGrid.this.addRequest(new RequestGrid.TubeRequest(this.currentTube, stack, color, amount, requestId));
         RequestGrid.RequestDetector detector = this.getDetector(color, stack.getItem());
         if (detector != null) {
            detector.markAmount(amount, this.currentTube.getRequestSource());
         }
      }

      @Override
      public int validateRequest(ItemStack stack, int originalRequest, DyeColor color) {
         if (!this.isLimited && !this.tested) {
            this.tested = true;

            for (IProviderTube provider : RequestGrid.this.providers) {
               this.currentProvider = provider.getProviderSource();
               provider.getItemsProvided(RequestGrid.this.requester);
            }
         }

         RequestGrid.RequestDetector detector = this.getDetector(color, stack.getItem());
         return detector == null ? 0 : Math.min(detector.getPresentCount(this.currentTube.getRequestSource()), originalRequest);
      }

      public RequestGrid.RequestDetector getDetector(DyeColor color, Item item) {
         if (!this.isLimited) {
            return this.itemsInNetwork.get(item);
         } else {
            Map<Item, RequestGrid.RequestDetector> detectors = this.limitedItemsInNetwork.get(color);
            if (detectors == null) {
               detectors = CollectionUtils.<Item, RequestGrid.RequestDetector>createLinkedMap();
               this.limitedItemsInNetwork.put(color, detectors);
               this.currentDyeColor = color;

               for (TubeGrid.TubePath path : RequestGrid.this.limiterPaths.getOrDefault(this.currentTube.getPosition().asLong(), Collections.emptyList())) {
                  if (path.validColors == -1 || color != null && (path.validColors & 1 << color.getId()) != 0) {
                     this.currentProvider = path.provider.getProviderSource();
                     path.provider.getItemsProvided(RequestGrid.this.requester);
                  }
               }

               this.currentDyeColor = null;
            }

            return detectors.get(item);
         }
      }

      public void accept(ItemStack t, int value) {
         if (!this.isLimited) {
            this.itemsInNetwork.computeIfAbsent(t.getItem(), RequestGrid.RequestDetector::new).add(value, this.currentProvider);
         } else {
            this.limitedItemsInNetwork
               .computeIfAbsent(this.currentDyeColor, this::create)
               .computeIfAbsent(t.getItem(), RequestGrid.RequestDetector::new)
               .add(value, this.currentProvider);
         }
      }

      private Map<Item, RequestGrid.RequestDetector> create(DyeColor ignored) {
         return CollectionUtils.<Item, RequestGrid.RequestDetector>createLinkedMap();
      }
   }
}
