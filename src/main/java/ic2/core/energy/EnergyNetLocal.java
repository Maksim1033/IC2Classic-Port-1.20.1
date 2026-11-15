package ic2.core.energy;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.PacketStats;
import ic2.api.energy.TransferStats;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IMultiEnergySource;
import ic2.api.energy.tile.IMultiEnergyTile;
import ic2.api.util.DirectionList;
import ic2.api.util.IC2DamageSource;
import ic2.core.IC2;
import ic2.core.entity.explosion.IC2Explosion;
import ic2.core.item.wearable.armor.HazmatArmor;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.SanityHelper;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EnergyNetLocal {
   public static final Predicate<LivingEntity> SHOCK_PREDICATE = EnergyNetLocal::isShockable;
   Level world;
   Long2ObjectMap<IEnergyTile[]> registeredTiles = new Long2ObjectOpenHashMap();
   Long2ObjectMap<IEnergySource> sources = new Long2ObjectLinkedOpenHashMap();
   Long2ObjectMap<IEnergySource> sortedSources = new Long2ObjectLinkedOpenHashMap();
   Map<IEnergyTile, EnergyNetLocal.Connectivity> connectivity = CollectionUtils.<IEnergyTile, EnergyNetLocal.Connectivity>createMap();
   Map<IEnergyTile, List<IEnergyTile>> cachedMultiTiles = CollectionUtils.<IEnergyTile, List<IEnergyTile>>createMap();
   Int2ObjectMap<EnergyNetGrid> grids = new Int2ObjectLinkedOpenHashMap();
   Object2IntMap<IEnergyConductor> cableToGrids = new Object2IntOpenHashMap();
   List<IEnergyTile[]> directConnections = new ObjectArrayList();
   IntSet dirtyGrids = new IntLinkedOpenHashSet();
   Object2LongOpenHashMap<IEnergyTile>[] tileStats = new Object2LongOpenHashMap[]{
      new Object2LongOpenHashMap(), new Object2LongOpenHashMap(), new Object2LongOpenHashMap(), new Object2LongOpenHashMap()
   };
   Random random = new Random();
   EnergyNetPaths energySourcePaths = new EnergyNetPaths();
   Predicate<IEnergyTile> remover = T -> !this.registeredTiles.containsKey(T.getPosition().asLong());
   int[] limitations = new int[]{-1, -1, -1, -1};
   Object2IntLinkedOpenHashMap<LivingEntity> entitiesToShock = new Object2IntLinkedOpenHashMap();
   boolean recalculateSources = false;

   EnergyNetLocal(Level world) {
      this.world = world;
   }

   public void setGridLimitations(int cables, int sources, int sinks, int complexity) {
      this.limitations[0] = cables;
      this.limitations[1] = sources;
      this.limitations[2] = sinks;
      this.limitations[3] = complexity;
   }

   public int isGridToLarge(IEnergyConductor tile) {
      if (tile == null) {
         return -1;
      } else {
         int gridId = this.cableToGrids.getOrDefault(tile, -1);
         if (gridId == -1) {
            return -1;
         } else {
            EnergyNetGrid grid = (EnergyNetGrid)this.grids.get(gridId);
            return grid == null ? -1 : grid.getGridToBigReason();
         }
      }
   }

   private static boolean isShockable(LivingEntity living) {
      if (living.isSpectator() || !living.isAlive()) {
         return false;
      } else if (HazmatArmor.isFullHazmatSuit(living)) {
         return false;
      } else {
         if (living instanceof Player player && player.isCreative()) {
            return false;
         }

         return true;
      }
   }

   public void printDimensionString() {
      if (!this.grids.isEmpty()) {
         Path path = IC2.CONFIG.getHandler().getCfgDir().resolve("enet");

         try {
            Files.createDirectories(path);
         } catch (Exception var7) {
            var7.printStackTrace();
         }

         StringJoiner joiner = new StringJoiner("\n");
         joiner.add("Local Grid: [" + this.world.dimension().location() + "]");
         joiner.add("Registered Tiles: " + this.registeredTiles.size());
         joiner.add("Sources: " + this.sources.size());
         joiner.add("Sinks: " + this.registeredTiles.values().stream().map(T -> (IEnergyTile)T[0]).filter(T -> T instanceof IEnergySink).count());
         joiner.add("Cables: " + this.cableToGrids.size());
         joiner.add("Grid Count: " + this.grids.size());
         joiner.add("");
         ObjectIterator e = this.grids.values().iterator();

         while (e.hasNext()) {
            EnergyNetGrid grid = (EnergyNetGrid)e.next();
            joiner.add(grid.generateDebugString());
         }

         try (BufferedWriter writer = Files.newBufferedWriter(path.resolve(toPascalCase(this.world.dimension().location().getPath()) + ".txt"))) {
            writer.append(joiner.toString());
         } catch (Exception var9) {
            var9.printStackTrace();
         }
      }
   }

   public static String toPascalCase(String input) {
      StringBuilder builder = new StringBuilder();

      for (String s : input.split("[\\s-_]")) {
         builder.append(SanityHelper.firstLetterUppercase(s)).append(" ");
      }

      return builder.substring(0, builder.length() - 1);
   }

   public void onTickStart() {
      if (!this.entitiesToShock.isEmpty()) {
         ObjectIterator var1 = Object2IntMaps.fastIterable(this.entitiesToShock).iterator();

         while (var1.hasNext()) {
            Entry<LivingEntity> entry = (Entry<LivingEntity>)var1.next();
            LivingEntity target = (LivingEntity)entry.getKey();
            int damage = (entry.getIntValue() + 63) / 64;
            if (target.isAlive() && damage > 0) {
               target.hurt(IC2DamageSource.newShockDamage(target), (float)damage);
               if (target instanceof Player player) {
                  player.awardStat(IC2Stats.CABLE_SHOCK_DAMAGE, damage);
               }
            }
         }

         this.entitiesToShock.clear();
      }
   }

   public void sortDependencies() {
      Map<IEnergySource, IEnergySource.SourceType> mappedSources = new Object2ObjectOpenHashMap(this.sources.size());
      Map<IEnergySource, Set<IEnergySource>> dependencies = new Object2ObjectOpenHashMap(this.sources.size());
      Object2IntMap<IEnergySource> depthMap = new Object2IntOpenHashMap();
      Set<IEnergySource> toVisit = new ObjectOpenHashSet(128);
      List<it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<IEnergySource>> dependencyLess = new ArrayList<>(this.sources.size());
      List<it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<IEnergySource>> dependencyWith = new ArrayList<>(this.sources.size());
      ObjectIterator comparator = this.sources.long2ObjectEntrySet().iterator();

      while (comparator.hasNext()) {
         it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<IEnergySource> entry = (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<IEnergySource>)comparator.next();
         IEnergySource source = (IEnergySource)entry.getValue();
         mappedSources.put(source, this.getType(source, toVisit));
         toVisit.clear();
         if (source instanceof IEnergySink sink) {
            for (EnergyNetLocal.EnergyPath path : this.energySourcePaths.getReverse(sink)) {
               dependencies.computeIfAbsent(path.source, T -> new ObjectOpenHashSet()).add(source);
            }
         }

         dependencyLess.add(entry);
         dependencyWith.add(entry);
      }

      for (IEnergySource source : dependencies.keySet()) {
         if (!depthMap.containsKey(source)) {
            depthMap.put(source, this.computeDepth(source, toVisit, depthMap, dependencies));
            toVisit.clear();
         }
      }

      dependencyLess.removeIf(T -> dependencies.containsKey(T.getValue()));
      dependencyLess.sort(Comparator.comparing(java.util.Map.Entry::getValue, Comparator.comparing(mappedSources::get)));
      dependencyWith.removeIf(T -> !dependencies.containsKey(T.getValue()));
      Comparator<it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<IEnergySource>> comparatorx = Comparator.comparing(
         java.util.Map.Entry::getValue, Comparator.comparing(depthMap::getInt)
      );
      dependencyWith.sort(comparatorx.thenComparing(java.util.Map.Entry::getValue, Comparator.comparing(mappedSources::get)));
      int index = 0;

      for (int m = dependencyLess.size(); index < m; index++) {
         it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<IEnergySource> entry = dependencyLess.get(index);
         if (mappedSources.get(entry.getValue()).ordinal() > IEnergySource.SourceType.INDIRECT_PASSIVE.ordinal()) {
            break;
         }

         this.sortedSources.put(entry.getLongKey(), (IEnergySource)entry.getValue());
      }

      dependencyWith.forEach(T -> this.sortedSources.put(T.getLongKey(), (IEnergySource)T.getValue()));

      for (int m = dependencyLess.size(); index < m; index++) {
         it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<IEnergySource> entry = dependencyLess.get(index);
         this.sortedSources.put(entry.getLongKey(), (IEnergySource)entry.getValue());
      }
   }

   private int computeDepth(
      IEnergySource source, Set<IEnergySource> visited, Object2IntMap<IEnergySource> resultMap, Map<IEnergySource, Set<IEnergySource>> dependencies
   ) {
      Set<IEnergySource> receivers = dependencies.get(source);
      if (receivers == null) {
         return 0;
      } else {
         visited.add(source);
         int max = 0;

         for (IEnergySource entry : receivers) {
            if (visited.add(entry)) {
               int value = resultMap.getOrDefault(entry, -1);
               if (value == -1) {
                  value = this.computeDepth(entry, visited, resultMap, dependencies);
                  resultMap.put(entry, value);
               }

               max = Math.max(max, value);
            }
         }

         return max;
      }
   }

   private IEnergySource.SourceType getType(IEnergySource source, Set<IEnergySource> visited) {
      visited.add(source);
      return source instanceof IEnergySink sink
         ? IEnergySource.SourceType.required(IEnergySource.SourceType.INDIRECT_PASSIVE, this.getRecursiveType(sink, visited))
         : source.getSourceType();
   }

   private IEnergySource.SourceType getRecursiveType(IEnergySink source, Set<IEnergySource> visited) {
      IEnergySource.SourceType type = IEnergySource.SourceType.UNKNOWN;

      for (EnergyNetLocal.EnergyPath path : this.energySourcePaths.getReverse(source)) {
         if (visited.add(path.source)) {
            type = type.merge(this.getType(path.source, visited));
         }
      }

      return type;
   }

   public void onTickStop() {
      if (!this.dirtyGrids.isEmpty()) {
         int[] array = this.dirtyGrids.toIntArray();
         this.dirtyGrids.clear();

         for (int j : array) {
            EnergyNetGrid grid = (EnergyNetGrid)this.grids.get(j);
            if (grid != null && !grid.cables.isEmpty()) {
               grid.processChanges();
            }
         }
      }

      if (!this.directConnections.isEmpty()) {
         Object2ObjectMap<IEnergyTile, Set<IEnergyTile>> sets = CollectionUtils.<IEnergyTile, Set<IEnergyTile>>createLinkedMap();

         for (IEnergyTile[] tiles : this.directConnections) {
            Set<IEnergyTile> receivers = (Set<IEnergyTile>)sets.get(tiles[0]);
            if (receivers == null) {
               receivers = CollectionUtils.<IEnergyTile>createSet();
               sets.put(tiles[0], receivers);
            }

            receivers.add(tiles[1]);
         }

         sets.keySet().removeIf(this.remover);
         ObjectIterator var11 = Object2ObjectMaps.fastIterable(sets).iterator();

         while (var11.hasNext()) {
            java.util.Map.Entry<IEnergyTile, Set<IEnergyTile>> entry = (java.util.Map.Entry<IEnergyTile, Set<IEnergyTile>>)var11.next();
            IEnergySource source = (IEnergySource)entry.getKey();
            this.energySourcePaths.addSourcePaths(source, this.discoverPaths(source, entry.getValue(), false, source.getMaxEnergyOutput()));
         }

         this.directConnections.clear();
         this.recalculateSources = true;
      }

      if (this.recalculateSources) {
         this.recalculateSources = false;
         this.sortedSources.clear();
         if (IC2.CONFIG.experimentalSourcesSorting.get()) {
            this.sortDependencies();
         } else {
            this.sortedSources.putAll(this.sources);
         }
      }

      if (!this.sources.isEmpty()) {
         ObjectIterator var9 = Long2ObjectMaps.fastIterable(this.sortedSources).iterator();

         while (var9.hasNext()) {
            it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<IEnergySource> entry = (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<IEnergySource>)var9.next();
            if (this.registeredTiles.containsKey(entry.getLongKey())) {
               IEnergySource source = (IEnergySource)entry.getValue();
               int offered = source.getProvidedEnergy();
               if (source instanceof IMultiEnergySource && ((IMultiEnergySource)source).hasMultiplePackets()) {
                  int packetCount = ((IMultiEnergySource)source).getPacketCount();

                  for (int i = 0; i < packetCount && offered >= 1; i++) {
                     int removed = offered - this.sendEnergyTo(entry.getLongKey(), source, offered);
                     if (removed <= 0) {
                        break;
                     }

                     source.consumeEnergy(removed);
                     offered = source.getProvidedEnergy();
                     this.addPacket(source, removed, false);
                  }
               } else {
                  int removed = offered - this.sendEnergyTo(entry.getLongKey(), source, offered);
                  if (removed > 0) {
                     source.consumeEnergy(removed);
                     this.addPacket(source, removed, false);
                  }
               }
            }
         }
      }
   }

   private boolean wouldBlowUp(EnergyNetLocal.EnergyPath path, int energyToSend) {
      return energyToSend > EnergyNet.INSTANCE.getPowerFromTier(path.target.getSinkTier()) ? true : energyToSend >= path.minConductorBreakdownEnergy;
   }

   private int sendEnergyTo(long key, IEnergySource source, int energyToSend) {
      if (!this.registeredTiles.containsKey(key)) {
         return energyToSend;
      } else {
         Set<EnergyNetLocal.EnergyPath> paths = this.energySourcePaths.get(source);
         if (paths.isEmpty()) {
            return energyToSend;
         } else {
            double totalInvLoss = 0.0;
            int energyWithLoss = energyToSend;
            boolean lossStop = false;
            boolean noRequestStop = false;
            ObjectList<EnergyNetLocal.EnergyPath> activePaths = new ObjectArrayList(paths.size());

            for (EnergyNetLocal.EnergyPath path : paths) {
               if (path.loss >= (double)energyToSend
                  || path.target.getRequestedEnergy() < 1
                  || IC2.CONFIG.energyEasyMode.get() && this.wouldBlowUp(path, energyToSend)) {
                  if (path.loss >= (double)energyToSend) {
                     lossStop = true;
                  } else {
                     noRequestStop = true;
                  }
               } else {
                  totalInvLoss += 1.0 / path.loss;
                  energyWithLoss -= (int)path.loss;
                  activePaths.add(path);
               }
            }

            if (activePaths.isEmpty()) {
               if (lossStop && !noRequestStop) {
                  source.onPacketFailed();
               }

               return energyToSend;
            } else {
               ObjectLists.shuffle(activePaths, this.random);

               while (activePaths.size() - energyWithLoss > 0 && !activePaths.isEmpty()) {
                  EnergyNetLocal.EnergyPath pathx = (EnergyNetLocal.EnergyPath)activePaths.remove(activePaths.size() - 1);
                  totalInvLoss -= 1.0 / pathx.loss;
                  energyWithLoss += (int)pathx.loss;
               }

               if (activePaths.isEmpty()) {
                  if (lossStop) {
                     source.onPacketFailed();
                  }

                  return energyToSend;
               } else {
                  Object2IntLinkedOpenHashMap<EnergyNetLocal.EnergyPath> suppliedPaths = new Object2IntLinkedOpenHashMap(activePaths.size());
                  int sourceTier = EnergyNet.INSTANCE.getPowerFromTier(source.getSourceTier());

                  while (!activePaths.isEmpty() && energyToSend > 0) {
                     int consumed = 0;
                     double newTotalInvLoss = 0.0;
                     ObjectList<EnergyNetLocal.EnergyPath> currentPaths = activePaths;
                     activePaths = new ObjectArrayList(activePaths.size());
                     ObjectListIterator entities = currentPaths.iterator();

                     while (entities.hasNext()) {
                        EnergyNetLocal.EnergyPath energyPath = (EnergyNetLocal.EnergyPath)entities.next();
                        IEnergySink target = energyPath.target;
                        int energyProvided = (int)Math.floor((double)Math.round((double)energyToSend / totalInvLoss / energyPath.loss * 100000.0) / 100000.0);
                        int energyLoss = (int)Math.floor(energyPath.loss);
                        if (energyProvided > energyLoss) {
                           int providing = energyProvided - energyLoss;
                           int adding = Math.min(providing, target.getRequestedEnergy());
                           if (adding > 0) {
                              if (providing > EnergyNet.INSTANCE.getPowerFromTier(target.getSinkTier())) {
                                 this.explodeTiles(target);
                              } else {
                                 double energyReturned = (double)target.acceptEnergy(energyPath.targetDirection, adding, sourceTier);
                                 if (energyReturned == 0.0) {
                                    if (target.getRequestedEnergy() >= 1) {
                                       activePaths.add(energyPath);
                                       newTotalInvLoss += 1.0 / energyPath.loss;
                                    }
                                 } else if (energyReturned >= (double)(energyProvided - energyLoss)) {
                                    energyReturned = (double)(energyProvided - energyLoss);
                                    IC2.LOGGER
                                       .warn(
                                          "API ERROR: {}, At{} didn't implement getRequestedEnergy() properly, no energy from acceptEnergy accepted although getRequestedEnergy() returned true: Requested: {}, Returned: {}",
                                          target.getClass().getSimpleName(),
                                          target.getPosition(),
                                          adding,
                                          energyReturned
                                       );
                                 }

                                 consumed += (int)((double)adding - energyReturned + (double)energyLoss);
                                 int energyInjected = (int)((double)adding - energyReturned);
                                 if (energyInjected > 0) {
                                    suppliedPaths.addTo(energyPath, energyInjected);
                                 }
                              }
                           }
                        } else {
                           activePaths.add(energyPath);
                           newTotalInvLoss += 1.0 / energyPath.loss;
                        }
                     }

                     if (consumed == 0 && !activePaths.isEmpty()) {
                        newTotalInvLoss -= 1.0 / ((EnergyNetLocal.EnergyPath)activePaths.remove(activePaths.size() - 1)).loss;
                     }

                     totalInvLoss = newTotalInvLoss;
                     energyToSend -= consumed;
                  }

                  ObjectIterator var31 = Object2IntMaps.fastIterable(suppliedPaths).iterator();

                  while (var31.hasNext()) {
                     Entry<EnergyNetLocal.EnergyPath> entry = (Entry<EnergyNetLocal.EnergyPath>)var31.next();
                     EnergyNetLocal.EnergyPath pathx = (EnergyNetLocal.EnergyPath)entry.getKey();
                     int supplied = entry.getIntValue();
                     pathx.totalEnergyConducted += (long)supplied;
                     this.addPacket(pathx.target, supplied, true);
                     if (supplied > pathx.minInsulationEnergyAbsorption) {
                        List<LivingEntity> entities = this.world
                           .getEntitiesOfClass(
                              LivingEntity.class,
                              new AABB(
                                 (double)(pathx.minX - 3),
                                 (double)(pathx.minY - 3),
                                 (double)(pathx.minZ - 3),
                                 (double)(pathx.maxX + 4),
                                 (double)(pathx.maxY + 4),
                                 (double)(pathx.maxZ + 4)
                              ),
                              SHOCK_PREDICATE
                           );
                        if (!entities.isEmpty()) {
                           Object2IntLinkedOpenHashMap<LivingEntity> shockMap = new Object2IntLinkedOpenHashMap();

                           for (IEnergyConductor cable : pathx.cables) {
                              int shockAbsorbed = cable.getInsulationEnergyAbsorption();
                              if (shockAbsorbed < supplied) {
                                 AABB conductorBox = cable.isLavaLogged() ? new AABB(cable.getPosition()) : new AABB(cable.getPosition()).inflate(1.0);
                                 AABB waterBox = cable.isWaterlogged() ? new AABB(cable.getPosition()).inflate(3.0) : null;

                                 for (LivingEntity entity : entities) {
                                    if (entity.getBoundingBox().intersects(conductorBox)
                                       || waterBox != null && entity.isInWaterOrRain() && entity.getBoundingBox().intersects(waterBox)) {
                                       int shockEnergy = supplied - shockAbsorbed;
                                       if (shockEnergy > 0 && shockMap.getInt(entity) < shockEnergy) {
                                          shockMap.put(entity, shockEnergy);
                                       }
                                    }
                                 }
                              }
                           }

                           if (!shockMap.isEmpty()) {
                              ObjectIterator var41 = Object2IntMaps.fastIterable(shockMap).iterator();

                              while (var41.hasNext()) {
                                 Entry<LivingEntity> shockEntry = (Entry<LivingEntity>)var41.next();
                                 this.entitiesToShock.addTo((LivingEntity)shockEntry.getKey(), shockEntry.getIntValue());
                              }
                           }
                        }
                     }

                     if (supplied >= pathx.minInsulationBreakdownEnergy) {
                        pathx.minInsulationEnergyAbsorption = 2147483647;

                        for (IEnergyConductor conductor : pathx.cables) {
                           if (supplied >= conductor.getInsulationBreakdownEnergy()) {
                              conductor.removeInsulation();
                           } else {
                              pathx.minInsulationEnergyAbsorption = Math.min(pathx.minInsulationEnergyAbsorption, conductor.getInsulationEnergyAbsorption());
                           }
                        }
                     }

                     if (supplied >= pathx.minConductorBreakdownEnergy) {
                        for (IEnergyConductor conductorx : pathx.cables) {
                           if (supplied >= conductorx.getConductorBreakdownEnergy()) {
                              this.removeTile(conductorx);
                              conductorx.removeConductor();
                           }
                        }
                     }
                  }

                  return energyToSend;
               }
            }
         }
      }
   }

   public void updateTile(IEnergyTile tile) {
      IEnergyTile[] tiles = (IEnergyTile[])this.registeredTiles.get(tile.getPosition().asLong());
      if (tiles != null && tiles[0] == tile) {
         this.removeTile(tile);
         this.addTile(tile);
      }
   }

   public void addTile(IEnergyTile tile) {
      if (tile instanceof IEnergySource || tile instanceof IEnergySink || tile instanceof IEnergyConductor) {
         if (tile instanceof IEnergySource source && (source.getSourceTier() > 13 || source.getSourceTier() < 0)) {
            IC2.LOGGER.warn("Tile [{}] has a Invalid Source Tier, Ignoring, Valid Range=0-13", tile.getClass());
            return;
         }

         if (tile instanceof IEnergySink sink && (sink.getSinkTier() > 13 || sink.getSinkTier() < 0)) {
            IC2.LOGGER.warn("Tile [{}] has a Invalid Sink Tier, Ignoring, Valid Range=0-13", tile.getClass());
            return;
         }

         if (tile instanceof IMultiEnergyTile) {
            this.addMultiTile((IMultiEnergyTile)tile);
         } else {
            this.addSimpleTile(tile);
         }
      }
   }

   private void addMultiTile(IMultiEnergyTile tile) {
      if (this.registeredTiles.containsKey(tile.getPosition().asLong())) {
         IC2.LOGGER.info("Double Registered EnergyTile at [World={}, Pos={}]", tile.getWorldObj().dimension().location(), tile.getPosition());
      } else {
         List<IEnergyTile> tiles = new ObjectArrayList(tile.getTiles());
         this.cachedMultiTiles.put(tile, tiles);

         for (IEnergyTile child : tiles) {
            long position = child.getPosition().asLong();
            if (!this.registeredTiles.containsKey(position)) {
               this.registeredTiles.put(position, new IEnergyTile[]{tile, child});
               this.update(child.getPosition());
            }
         }

         this.addToGrids(tile);
         if (tile instanceof IEnergySource) {
            this.sources.put(tile.getPosition().asLong(), (IEnergySource)tile);
            this.recalculateSources = true;
         }
      }
   }

   private void addSimpleTile(IEnergyTile tile) {
      long position = tile.getPosition().asLong();
      if (this.registeredTiles.containsKey(position)) {
         IC2.LOGGER.info("Double Registered EnergyTile at [World={}, Pos={}]", tile.getWorldObj().dimension().location(), tile.getPosition());
      } else {
         this.registeredTiles.put(position, new IEnergyTile[]{tile, tile});
         this.update(tile.getPosition());
         this.addToGrids(tile);
         if (tile instanceof IEnergySource) {
            this.sources.put(tile.getPosition().asLong(), (IEnergySource)tile);
            this.recalculateSources = true;
         }
      }
   }

   private void addToGrids(IEnergyTile tile) {
      EnergyNetLocal.Connectivity connections = new EnergyNetLocal.Connectivity(tile, this);
      this.connectivity.put(tile, connections);
      boolean cable = tile instanceof IEnergyConductor;
      if (cable) {
         IntSortedSet toCombine = new IntLinkedOpenHashSet();
         List<IEnergySink> sinksToAdd = CollectionUtils.<IEnergySink>createList();
         List<IEnergySource> sourcesToAdd = CollectionUtils.<IEnergySource>createList();
         if (tile instanceof IEnergySource) {
            sourcesToAdd.add((IEnergySource)tile);
         }

         if (tile instanceof IEnergySink) {
            sinksToAdd.add((IEnergySink)tile);
         }

         for (EnergyNetLocal.EnergyLink link : connections.getReceivers()) {
            IEnergyTile target = link.target;
            if (target instanceof IEnergyConductor) {
               toCombine.add(this.cableToGrids.getInt(target));
            }

            if (target instanceof IEnergySink) {
               sinksToAdd.add((IEnergySink)target);
            }
         }

         for (EnergyNetLocal.EnergyLink link : connections.getEmitters()) {
            IEnergyTile targetx = link.target;
            if (targetx instanceof IEnergyConductor) {
               toCombine.add(this.cableToGrids.getInt(targetx));
            }

            if (targetx instanceof IEnergySource) {
               sourcesToAdd.add((IEnergySource)targetx);
            }
         }

         if (toCombine.isEmpty()) {
            EnergyNetGrid grid = EnergyNetGrid.createNewGrid(this);
            grid.addConductor((IEnergyConductor)tile);
            grid.addSinks(sinksToAdd);
            grid.addSource(sourcesToAdd);
            this.grids.put(grid.getGridID(), grid);
         } else if (toCombine.size() == 1) {
            EnergyNetGrid grid = (EnergyNetGrid)this.grids.get(toCombine.firstInt());
            grid.addConductor((IEnergyConductor)tile);
            grid.addSinks(sinksToAdd);
            grid.addSource(sourcesToAdd);
         } else {
            EnergyNetGrid grid = EnergyNetGrid.createNewGrid(this);
            IntBidirectionalIterator var24 = toCombine.iterator();

            while (var24.hasNext()) {
               int id = (Integer)var24.next();
               EnergyNetGrid subGrid = (EnergyNetGrid)this.grids.remove(id);
               grid.addGrid(subGrid);
               subGrid.destroyGrid();
            }

            grid.addConductor((IEnergyConductor)tile);
            grid.addSource(sourcesToAdd);
            grid.addSinks(sinksToAdd);
            grid.finishCombining();
            this.grids.put(grid.getGridID(), grid);
         }
      } else {
         if (tile instanceof IEnergySink sink) {
            for (EnergyNetLocal.EnergyLink link : connections.getEmitters()) {
               IEnergyTile targetTile = link.target;
               if (targetTile instanceof IEnergyConductor) {
                  ((EnergyNetGrid)this.grids.get(this.cableToGrids.getInt(targetTile))).addSink(sink);
               }

               if (targetTile instanceof IEnergySource) {
                  this.directConnections.add(new IEnergyTile[]{targetTile, sink});
               }
            }
         }

         if (tile instanceof IEnergySource source) {
            for (EnergyNetLocal.EnergyLink link : connections.getReceivers()) {
               IEnergyTile targetTilex = link.target;
               if (targetTilex instanceof IEnergyConductor) {
                  ((EnergyNetGrid)this.grids.get(this.cableToGrids.getInt(targetTilex))).addSource(source);
               }

               if (targetTilex instanceof IEnergySink) {
                  this.directConnections.add(new IEnergyTile[]{source, targetTilex});
               }
            }
         }
      }
   }

   void splitGrid(EnergyNetGrid grid) {
      this.recalculateSources = true;
      this.grids.remove(grid.getGridID());
      List<EnergyNetGrid> gridsToAdd = new ArrayList<>();
      Set<IEnergyConductor> left = CollectionUtils.<IEnergyConductor>createLinkedSet();
      left.addAll(grid.cables.keySet());

      while (!left.isEmpty()) {
         EnergyNetGrid newGrid = EnergyNetGrid.createNewGrid(this);
         newGrid.setFlag(2);
         PriorityQueue<IEnergyConductor> toProcess = CollectionUtils.createInsertionQueue();
         toProcess.enqueue(left.iterator().next());

         while (!toProcess.isEmpty()) {
            IEnergyConductor cable = (IEnergyConductor)toProcess.dequeue();
            if (this.registeredTiles.containsKey(cable.getPosition().asLong())) {
               left.remove(cable);
               newGrid.addConductor(cable);
               EnergyNetLocal.Connectivity targets = this.connectivity.get(cable);

               for (EnergyNetLocal.EnergyLink link : targets.getReceivers()) {
                  IEnergyTile tile = link.target;
                  if (tile instanceof IEnergyConductor && left.remove(tile)) {
                     toProcess.enqueue((IEnergyConductor)tile);
                  }

                  if (tile instanceof IEnergySink) {
                     newGrid.addSink((IEnergySink)tile);
                  }
               }

               for (EnergyNetLocal.EnergyLink link : targets.getEmitters()) {
                  IEnergyTile tilex = link.target;
                  if (tilex instanceof IEnergyConductor && left.remove(tilex)) {
                     toProcess.enqueue((IEnergyConductor)tilex);
                  }

                  if (tilex instanceof IEnergySource) {
                     newGrid.addSource((IEnergySource)tilex);
                  }
               }
            }
         }

         gridsToAdd.add(newGrid);
         this.grids.put(newGrid.getGridID(), newGrid);
         newGrid.subPaths.transferPaths(grid.subPaths.getSubPaths(newGrid.sources, newGrid.cables.keySet(), newGrid.newSinks));
         newGrid.finishSplit();
      }

      grid.subPaths.reset();

      for (EnergyNetGrid newGrid : gridsToAdd) {
         newGrid.subPaths.addSubSet();
      }
   }

   public void removeTile(IEnergyTile tile) {
      if (tile instanceof IMultiEnergyTile) {
         this.removeMultiTile((IMultiEnergyTile)tile);
      } else {
         this.removeSimpleTile(tile);
      }

      for (int i = 0; i < 4; i++) {
         this.tileStats[i].removeLong(tile);
      }
   }

   private void removeMultiTile(IMultiEnergyTile tile) {
      if (this.registeredTiles.containsKey(tile.getPosition().asLong())) {
         for (IEnergyTile child : this.cachedMultiTiles.remove(tile)) {
            long position = child.getPosition().asLong();
            if (this.registeredTiles.containsKey(position)) {
               this.registeredTiles.remove(position);
               this.update(child.getPosition());
            }
         }

         this.removeFromGrids(tile);
         if (tile instanceof IEnergySource) {
            this.sources.remove(tile.getPosition().asLong());
            this.recalculateSources = true;
         }
      }
   }

   private void removeSimpleTile(IEnergyTile tile) {
      long position = tile.getPosition().asLong();
      if (this.registeredTiles.containsKey(position)) {
         IEnergyTile stored = ((IEnergyTile[])this.registeredTiles.remove(position))[0];
         this.update(stored.getPosition());
         this.removeFromGrids(stored);
         if (tile instanceof IEnergySource) {
            this.sources.remove(position);
            this.recalculateSources = true;
         }
      }
   }

   private void removeFromGrids(IEnergyTile tile) {
      EnergyNetLocal.Connectivity connection = this.connectivity.remove(tile);
      connection.destroy(this);
      if (tile instanceof IEnergyConductor) {
         EnergyNetGrid grid = (EnergyNetGrid)this.grids.get(this.cableToGrids.removeInt(tile));
         if (tile instanceof IEnergySource source) {
            grid.removeSource(source);
            this.energySourcePaths.removeSource(source);
         }

         if (tile instanceof IEnergySink sink) {
            grid.removeSink(sink);
            this.energySourcePaths.removeSink(sink);
         }

         Set<IEnergyConductor> wires = CollectionUtils.<IEnergyConductor>createLinkedSet();

         for (EnergyNetLocal.EnergyLink link : connection.getReceivers()) {
            IEnergyTile target = link.target;
            if (target instanceof IEnergyConductor) {
               wires.add((IEnergyConductor)target);
            }

            if (target instanceof IEnergySink && !this.hasMoreConnections(target, grid.getGridID())) {
               grid.removeSink((IEnergySink)target);
            }
         }

         for (EnergyNetLocal.EnergyLink link : connection.getEmitters()) {
            IEnergyTile targetx = link.target;
            if (targetx instanceof IEnergyConductor) {
               wires.add((IEnergyConductor)targetx);
            }

            if (targetx instanceof IEnergySource && !this.hasMoreConnections(targetx, grid.getGridID())) {
               grid.removeSource((IEnergySource)targetx);
            }
         }

         if (wires.isEmpty()) {
            grid.removeConductor((IEnergyConductor)tile);
            if (grid.cables.isEmpty()) {
               this.grids.remove(grid.getGridID());
            }
         } else if (wires.size() == 1) {
            grid.removeConductor((IEnergyConductor)tile);
         } else {
            grid.removeConductor((IEnergyConductor)tile);
            grid.markSplit();
         }
      } else {
         if (tile instanceof IEnergySource) {
            for (EnergyNetLocal.EnergyLink entry : connection.getReceivers()) {
               IEnergyTile targetxx = entry.target;
               if (targetxx instanceof IEnergyConductor) {
                  ((EnergyNetGrid)this.grids.get(this.cableToGrids.getInt(targetxx))).removeSource((IEnergySource)tile);
               }
            }

            this.energySourcePaths.removeSource((IEnergySource)tile);
         }

         if (tile instanceof IEnergySink) {
            for (EnergyNetLocal.EnergyLink entryx : connection.getEmitters()) {
               IEnergyTile targetxx = entryx.target;
               if (targetxx instanceof IEnergyConductor) {
                  ((EnergyNetGrid)this.grids.get(this.cableToGrids.getInt(targetxx))).removeSink((IEnergySink)tile);
               }
            }

            this.energySourcePaths.removeSink((IEnergySink)tile);
         }
      }

      connection.clear();
   }

   IEnergyTile[] get(BlockPos pos) {
      return (IEnergyTile[])this.registeredTiles.get(pos.asLong());
   }

   void update(BlockPos pos) {
      for (Direction facing : DirectionList.ALL) {
         BlockPos sidedPos = pos.relative(facing);
         if (this.world.isLoaded(sidedPos)) {
            this.world.neighborChanged(sidedPos, Blocks.AIR, pos);
         }
      }

      if (this.world.isLoaded(pos)) {
         this.world.neighborChanged(pos, Blocks.AIR, pos);
      }
   }

   boolean hasMoreConnections(IEnergyTile tile, int id) {
      EnergyNetLocal.Connectivity con = this.connectivity.get(tile);
      if (con == null) {
         return false;
      } else {
         for (EnergyNetLocal.EnergyLink link : con.getAll()) {
            if (link.target instanceof IEnergyConductor && this.cableToGrids.getInt(link.target) == id) {
               return true;
            }
         }

         return false;
      }
   }

   List<EnergyNetLocal.EnergyLink> getConnectedTiles(IEnergyTile tile, boolean reverse) {
      List<EnergyNetLocal.EnergyLink> links = new ArrayList<>();
      List<IEnergyTile> tiles = this.cachedMultiTiles.getOrDefault(tile, ObjectLists.singleton(tile));

      for (Direction dir : DirectionList.ALL) {
         for (IEnergyTile other : tiles) {
            IEnergyTile[] connection = this.get(other.getPosition().relative(dir));
            if (connection != null && connection[0] != tile) {
               if (reverse) {
                  IEnergyEmitter sender = this.getEmitter(tile, other);
                  IEnergyAcceptor acceptor = this.getAcceptor(connection[0], connection[1]);
                  if (sender != null && acceptor != null && sender.canEmitEnergy(acceptor, dir) && acceptor.canAcceptEnergy(sender, dir.getOpposite())) {
                     links.add(new EnergyNetLocal.EnergyLink(tile, other.getPosition(), connection[0], connection[1].getPosition(), dir.getOpposite()));
                  }
               } else {
                  IEnergyEmitter sender = this.getEmitter(connection[0], connection[1]);
                  IEnergyAcceptor acceptor = this.getAcceptor(tile, other);
                  if (sender != null && acceptor != null && sender.canEmitEnergy(acceptor, dir.getOpposite()) && acceptor.canAcceptEnergy(sender, dir)) {
                     links.add(new EnergyNetLocal.EnergyLink(tile, other.getPosition(), connection[0], connection[1].getPosition(), dir.getOpposite()));
                  }
               }
            }
         }
      }

      return links;
   }

   private Set<EnergyNetLocal.EnergyLink> getConnectivity(IEnergyTile actually, boolean reverse) {
      return this.connectivity.getOrDefault(actually, EnergyNetLocal.Connectivity.NULL).get(reverse);
   }

   public List<EnergyNetLocal.EnergyPath> discoverPaths(IEnergyTile source, Set<? extends IEnergyTile> targets, boolean reverse, int lossLimit) {
      Map<IEnergyTile, EnergyNetLocal.EnergyBlock> reachedLinks = CollectionUtils.<IEnergyTile, EnergyNetLocal.EnergyBlock>createLinkedMap();
      LinkedList<IEnergyTile> toCheck = new LinkedList<>();
      toCheck.add(source);
      List<IEnergyTile> reached = new ObjectArrayList();

      while (!toCheck.isEmpty()) {
         IEnergyTile tile = toCheck.pollFirst();
         EnergyNetLocal.EnergyBlock block = reachedLinks.get(tile);
         double loss = block == null ? 0.0 : block.loss;

         for (EnergyNetLocal.EnergyLink link : this.getConnectivity(tile, reverse)) {
            IEnergyTile possible = link.target;
            if (possible != source) {
               double extraLoss = 0.0;
               if (possible instanceof IEnergyConductor) {
                  extraLoss = Math.max(((IEnergyConductor)possible).getConductionLoss(), 0.001);
                  if (loss + extraLoss >= (double)lossLimit) {
                     continue;
                  }
               }

               block = reachedLinks.get(possible);
               if (block == null || !(block.loss <= loss + extraLoss)) {
                  if (!reverse && possible instanceof IEnergySink) {
                     if (!targets.isEmpty() && !targets.contains(possible)) {
                        continue;
                     }

                     reached.add(possible);
                  } else if (reverse && possible instanceof IEnergySource) {
                     if (!targets.isEmpty() && !targets.contains(possible)) {
                        continue;
                     }

                     reached.add(possible);
                  }

                  reachedLinks.put(possible, new EnergyNetLocal.EnergyBlock(link, loss + extraLoss));
                  if (possible instanceof IEnergyConductor) {
                     toCheck.remove(possible);
                     toCheck.add(possible);
                  }
               }
            }
         }
      }

      List<EnergyNetLocal.EnergyPath> energyPaths = new ObjectArrayList(reached.size());
      Iterator var19 = reached.iterator();

      while (true) {
         EnergyNetLocal.EnergyBlock energyBlockLink;
         IEnergyTile tile;
         EnergyNetLocal.EnergyPath path;
         while (true) {
            if (!var19.hasNext()) {
               return energyPaths;
            }

            tile = (IEnergyTile)var19.next();
            if (reverse && tile instanceof IEnergySource || !reverse && tile instanceof IEnergySink) {
               energyBlockLink = reachedLinks.get(tile);
               if (energyBlockLink != null) {
                  path = new EnergyNetLocal.EnergyPath();
                  path.loss = Math.max(0.1, energyBlockLink.loss);
                  if (reverse) {
                     if (path.loss > (double)((IEnergySource)tile).getMaxEnergyOutput()) {
                        continue;
                     }

                     path.source = (IEnergySource)tile;
                     break;
                  }

                  path.target = (IEnergySink)tile;
                  path.targetDirection = energyBlockLink.direction.getOpposite();
                  break;
               }
            }
         }

         path.pathWalked.add(tile.getPosition().asLong());
         path.pathWalked.add(energyBlockLink.targetPos.asLong());

         while (true) {
            tile = energyBlockLink.source;
            if (tile == source) {
               if (!reverse && source instanceof IEnergySource) {
                  path.source = (IEnergySource)source;
                  path.pathWalked.add(energyBlockLink.sourcePos.asLong());
                  path.pathWalked.add(energyBlockLink.source.getPosition().asLong());
                  Collections.reverse(path.cables);
                  Collections.reverse(path.pathWalked);
                  break;
               }

               if (reverse && source instanceof IEnergySink) {
                  path.target = (IEnergySink)source;
                  path.targetDirection = energyBlockLink.direction;
                  path.pathWalked.add(energyBlockLink.sourcePos.asLong());
                  path.pathWalked.add(energyBlockLink.source.getPosition().asLong());
               }
               break;
            }

            if (!(tile instanceof IEnergyConductor energyConductor)) {
               break;
            }

            BlockPos pos = energyBlockLink.sourcePos;
            path.pathWalked.add(pos.asLong());
            path.minX = Math.min(path.minX, pos.getX());
            path.minY = Math.min(path.minY, pos.getY());
            path.minZ = Math.min(path.minZ, pos.getZ());
            path.maxX = Math.max(path.maxX, pos.getX());
            path.maxY = Math.max(path.maxY, pos.getY());
            path.maxZ = Math.max(path.maxZ, pos.getZ());
            path.cables.add(energyConductor);
            path.cableCheck.add(energyConductor);
            path.minInsulationEnergyAbsorption = Math.min(path.minInsulationEnergyAbsorption, energyConductor.getInsulationEnergyAbsorption());
            path.minInsulationBreakdownEnergy = Math.min(path.minInsulationBreakdownEnergy, energyConductor.getInsulationBreakdownEnergy());
            path.minConductorBreakdownEnergy = Math.min(path.minConductorBreakdownEnergy, energyConductor.getConductorBreakdownEnergy());
            energyBlockLink = reachedLinks.get(tile);
            if (energyBlockLink == null) {
               IC2.PLATFORM
                  .displayError(
                     "An energy network pathfinding entry is corrupted.\nThis could happen due to incorrect Minecraft behavior or a bug.\n\n(Technical information: energyBlockLink, tile entities below)\nE: "
                        + source
                        + " ("
                        + source.getPosition()
                        + ")\nC: "
                        + tile
                        + " ("
                        + pos
                        + ")\nR: "
                        + path.target
                        + " ("
                        + path.target.getPosition()
                        + ")"
                  );
               break;
            }
         }

         if (path.target != null && path.source != null) {
            energyPaths.add(path);
         }
      }
   }

   IEnergyEmitter getEmitter(IEnergyTile main, IEnergyTile sub) {
      return (IEnergyEmitter)(sub instanceof IEnergyEmitter ? sub : (main instanceof IEnergyEmitter ? main : null));
   }

   IEnergyAcceptor getAcceptor(IEnergyTile main, IEnergyTile sub) {
      return (IEnergyAcceptor)(sub instanceof IEnergyAcceptor ? sub : (main instanceof IEnergyAcceptor ? main : null));
   }

   void markGridDirty(int id) {
      this.dirtyGrids.add(id);
   }

   void addConductorToGrid(IEnergyConductor conduct, int gridID) {
      this.cableToGrids.put(conduct, gridID);
   }

   void addPacket(IEnergyTile packet, int energy, boolean accept) {
      int index = accept ? 1 : 0;
      this.tileStats[index].addTo(packet, (long)energy);
      this.tileStats[index + 2].addTo(packet, 1L);
   }

   void explodeTiles(IEnergyTile tile) {
      List<BlockPos> positionsList = new ArrayList<>();

      for (IEnergyTile entry : this.cachedMultiTiles.getOrDefault(tile, ObjectLists.singleton(tile))) {
         positionsList.add(entry.getPosition());
      }

      this.removeTile(tile);

      for (BlockPos pos : positionsList) {
         this.world.removeBlock(pos, false);
      }

      for (BlockPos pos : positionsList) {
         IC2Explosion explosion = new IC2Explosion(this.world, null, Vec3.atLowerCornerOf(pos).add(0.5, 0.5, 0.5), 2.5F, 0.75F, IC2DamageSource.ELECTRICITY);
         explosion.doExplosion();
      }
   }

   public TransferStats getStats(IEnergyTile tile) {
      long in = 0L;
      long out = 0L;
      long lossIn = 0L;
      long lossOut = 0L;
      if (tile instanceof IEnergySource) {
         for (EnergyNetLocal.EnergyPath path : this.energySourcePaths.get((IEnergySource)tile)) {
            out += path.totalEnergyConducted;
            lossOut += (long)((int)path.loss);
         }
      }

      if (tile instanceof IEnergySink) {
         for (EnergyNetLocal.EnergyPath path : this.energySourcePaths.getReverse((IEnergySink)tile)) {
            in += path.totalEnergyConducted;
            lossIn += (long)((int)path.loss);
         }
      }

      if (tile instanceof IEnergyConductor) {
         EnergyNetGrid grid = (EnergyNetGrid)this.grids.get(this.cableToGrids.getInt(tile));
         if (grid != null) {
            long cableIn = 0L;
            double count = 0.0;

            for (EnergyNetLocal.EnergyPath path : grid.subPaths.getPathsForConductor((IEnergyConductor)tile)) {
               in += path.totalEnergyConducted;
               out += path.totalEnergyConducted;
               cableIn += (long)((int)path.loss);
               count++;
            }

            lossIn += (long)Math.ceil((double)cableIn / count);
         }
      }

      return new TransferStats(in, out, lossIn, lossOut);
   }

   public List<PacketStats> getPackets(IEnergyTile tile) {
      Set<IEnergySource> sources = CollectionUtils.<IEnergySource>createLinkedSet();
      Set<IEnergySink> sinks = CollectionUtils.<IEnergySink>createLinkedSet();
      if (tile instanceof IEnergySource) {
         sources.add((IEnergySource)tile);
      }

      if (tile instanceof IEnergySink) {
         sinks.add((IEnergySink)tile);
      }

      if (tile instanceof IEnergyConductor) {
         EnergyNetGrid grid = (EnergyNetGrid)this.grids.get(this.cableToGrids.getInt(tile));
         if (grid != null) {
            for (EnergyNetLocal.EnergyPath path : grid.subPaths.getPathsForConductor((IEnergyConductor)tile)) {
               sources.add(path.source);
               sinks.add(path.target);
            }
         }
      }

      List<PacketStats> stats = new ArrayList<>();

      for (IEnergySource source : sources) {
         stats.add(new PacketStats(source, this.tileStats[2].getLong(source), this.tileStats[0].getLong(source), false));
      }

      for (IEnergySink sink : sinks) {
         stats.add(new PacketStats(sink, this.tileStats[3].getLong(sink), this.tileStats[1].getLong(sink), true));
      }

      return stats;
   }

   static class Connectivity {
      private static final EnergyNetLocal.Connectivity NULL = new EnergyNetLocal.Connectivity();
      Set<EnergyNetLocal.EnergyLink> receivers = CollectionUtils.<EnergyNetLocal.EnergyLink>createLinkedSet();
      Set<EnergyNetLocal.EnergyLink> emitters = CollectionUtils.<EnergyNetLocal.EnergyLink>createLinkedSet();
      Set<EnergyNetLocal.EnergyLink> all = CollectionUtils.<EnergyNetLocal.EnergyLink>createLinkedSet();

      private Connectivity() {
      }

      public Connectivity(IEnergyTile tile, EnergyNetLocal local) {
         for (EnergyNetLocal.EnergyLink target : local.getConnectedTiles(tile, false)) {
            this.emitters.add(target);
            this.all.add(target);
            EnergyNetLocal.Connectivity other = local.connectivity.get(target.target);
            if (other == null) {
               IC2.LOGGER.info("Found a Non Connectivity Set Tile: {}", target.target);
            } else {
               EnergyNetLocal.EnergyLink inv = target.invert();
               other.receivers.add(inv);
               other.all.add(inv);
            }
         }

         for (EnergyNetLocal.EnergyLink targetx : local.getConnectedTiles(tile, true)) {
            this.receivers.add(targetx);
            this.all.add(targetx);
            EnergyNetLocal.Connectivity other = local.connectivity.get(targetx.target);
            if (other == null) {
               IC2.LOGGER.info("Found a Non Connectivity Set Tile: {}", targetx.target);
            } else {
               EnergyNetLocal.EnergyLink inv = targetx.invert();
               other.emitters.add(inv);
               other.all.add(inv);
            }
         }
      }

      public Set<EnergyNetLocal.EnergyLink> get(boolean reverse) {
         return reverse ? this.emitters : this.receivers;
      }

      public void destroy(EnergyNetLocal local) {
         for (EnergyNetLocal.EnergyLink link : this.all) {
            EnergyNetLocal.Connectivity other = local.connectivity.get(link.target);
            if (other != null) {
               EnergyNetLocal.EnergyLink otherLink = link.invert();
               other.all.remove(otherLink);
               other.receivers.remove(otherLink);
               other.emitters.remove(otherLink);
            }
         }
      }

      public void clear() {
         this.emitters.clear();
         this.receivers.clear();
         this.all.clear();
      }

      public Set<EnergyNetLocal.EnergyLink> getAll() {
         return this.all;
      }

      public Set<EnergyNetLocal.EnergyLink> getEmitters() {
         return this.emitters;
      }

      public Set<EnergyNetLocal.EnergyLink> getReceivers() {
         return this.receivers;
      }

      @Override
      public String toString() {
         return "Connections: " + this.all.toString();
      }
   }

   static class EnergyBlock {
      IEnergyTile source;
      BlockPos sourcePos;
      IEnergyTile target;
      BlockPos targetPos;
      Direction direction;
      double loss;

      public EnergyBlock(EnergyNetLocal.EnergyLink link, double loss) {
         this.source = link.source;
         this.sourcePos = link.sourcePos;
         this.target = link.target;
         this.targetPos = link.targetPos;
         this.direction = link.direction;
         this.loss = loss;
      }
   }

   static class EnergyLink {
      IEnergyTile source;
      BlockPos sourcePos;
      IEnergyTile target;
      BlockPos targetPos;
      Direction direction;
      EnergyNetLocal.EnergyLink inverted;

      private EnergyLink() {
      }

      public EnergyLink(IEnergyTile source, BlockPos sourcePos, IEnergyTile target, BlockPos targetPos, Direction direction) {
         this.source = source;
         this.sourcePos = sourcePos;
         this.target = target;
         this.targetPos = targetPos;
         this.direction = direction;
         this.inverted = new EnergyNetLocal.EnergyLink();
         this.inverted.source = target;
         this.inverted.sourcePos = targetPos;
         this.inverted.target = source;
         this.inverted.targetPos = sourcePos;
         this.inverted.direction = direction.getOpposite();
         this.inverted.inverted = this;
      }

      public EnergyNetLocal.EnergyLink invert() {
         return this.inverted;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.direction, this.targetPos);
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else {
            return !(obj instanceof EnergyNetLocal.EnergyLink other) ? false : other.direction == this.direction && other.targetPos.equals(this.targetPos);
         }
      }

      @Override
      public String toString() {
         return "Link: Source[" + this.source + "(" + this.sourcePos + ")], Target[" + this.target + "(" + this.targetPos + ")]";
      }
   }

   public static class EnergyPath {
      IEnergySource source = null;
      IEnergySink target = null;
      Direction targetDirection = null;
      List<IEnergyConductor> cables = new ArrayList<>();
      Set<IEnergyConductor> cableCheck = CollectionUtils.<IEnergyConductor>createSet();
      LongList pathWalked = new LongArrayList();
      int minX = 2147483647;
      int minY = 2147483647;
      int minZ = 2147483647;
      int maxX = -2147483648;
      int maxY = -2147483648;
      int maxZ = -2147483648;
      int minInsulationEnergyAbsorption = 2147483647;
      int minInsulationBreakdownEnergy = 2147483647;
      int minConductorBreakdownEnergy = 2147483647;
      long totalEnergyConducted = 0L;
      double loss = 0.0;

      EnergyPath() {
      }

      @Override
      public int hashCode() {
         return this.source.hashCode() + this.target.hashCode() * 31;
      }

      @Override
      public boolean equals(Object obj) {
         return !(obj instanceof EnergyNetLocal.EnergyPath path) ? false : path.source == this.source && path.target == this.target;
      }
   }
}
