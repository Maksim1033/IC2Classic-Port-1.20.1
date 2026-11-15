function initializeCoreMod() {
    Opcodes = Java.type("org.objectweb.asm.Opcodes");
    ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");

    InsnList = Java.type("org.objectweb.asm.tree.InsnList");
    LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");

    FieldNode = Java.type("org.objectweb.asm.tree.FieldNode");
    MethodNode = Java.type("org.objectweb.asm.tree.MethodNode");

    AbstractInsnNode = Java.type("org.objectweb.asm.tree.AbstractInsnNode");
    InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
    VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
    FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
    MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
    JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
    TypeInsnNode = Java.type("org.objectweb.asm.tree.TypeInsnNode");
    FrameNode = Java.type("org.objectweb.asm.tree.FrameNode");

    return {
        'betterarmorcalculations': {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.world.entity.player.Player",
                "methodName": ASMAPI.mapMethod("m_6475_"), //"actuallyHurt",
                "methodDesc": "(Lnet/minecraft/world/damagesource/DamageSource;F)V"
            },
            "transformer": function (methodNode) {
                var applyArmor = ASMAPI.buildMethodCall(
                    "ic2/core/platform/corehacks/ASMHacks",
                    "applyArmor",
                    "(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/damagesource/DamageSource;F)F",
                    ASMAPI.MethodType.STATIC
                );

                var armorCalc = null;
                for (var curr = methodNode.instructions.getFirst(); curr !== methodNode.instructions.getLast(); curr = curr.getNext()) {
                    if (curr.getOpcode() === Opcodes.INVOKEVIRTUAL) {
                        if (curr.owner === "net/minecraft/world/entity/player/Player" && curr.name === ASMAPI.mapMethod("m_21161_")/*"getDamageAfterArmorAbsorb"*/ && curr.desc === "(Lnet/minecraft/world/damagesource/DamageSource;F)F") {
                            armorCalc = curr;
                            break;
                        }
                    }
                }

                if (armorCalc == null) {
                    throw "net.minecraft.world.entity.LivingEntity.getDamageAfterArmorAbsorb call not found in net.minecraft.world.entity.player.Player.actuallyHurt";
                } else {
                    var toInsert = new InsnList();
                    toInsert.add(applyArmor);
                    toInsert.add(new VarInsnNode(Opcodes.FSTORE, 2));
                    toInsert.add(new VarInsnNode(Opcodes.FLOAD, 2));
                    toInsert.add(new InsnNode(Opcodes.FCONST_0));
                    toInsert.add(new InsnNode(Opcodes.FCMPG));
                    toInsert.add(new JumpInsnNode(Opcodes.IFGT, new LabelNode(armorCalc.getNext().getNext().getLabel())));
                    toInsert.add(new InsnNode(Opcodes.RETURN));

                    methodNode.instructions.remove(armorCalc.getNext());
                    methodNode.instructions.insert(armorCalc, toInsert);
                    methodNode.instructions.remove(armorCalc);
                }
                return methodNode;
            }
        },
        'thornsArmorCalc': {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.world.item.enchantment.ThornsEnchantment",
                "methodName": ASMAPI.mapMethod("m_7675_"), //"doPostHurt",
                "methodDesc": "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/Entity;I)V"
            },
            "transformer": function (methodNode) {
                var damageArmorMain = ASMAPI.buildMethodCall(
                    "ic2/core/platform/corehacks/ASMHacks",
                    "thornsSelfArmorDamage",
                    "(Ljava/util/Map$Entry;ILnet/minecraft/world/entity/LivingEntity;)V",
                    ASMAPI.MethodType.STATIC
                );

                var damageItemMain = null;
                for (var curr = methodNode.instructions.getFirst(); curr !== methodNode.instructions.getLast(); curr = curr.getNext()) {
                    if (curr.getOpcode() === Opcodes.INVOKEVIRTUAL) {
                        if (curr.owner === "net/minecraft/world/item/ItemStack" && curr.name === ASMAPI.mapMethod("m_41622_") /*"hurtAndBreak"*/ && curr.desc === "(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V") {
                                damageItemMain = curr;
                                break;
                        }
                    }
                }

                if (damageItemMain == null) {
                    throw "net.minecraft.world.item.ItemStack.hurtAndBreak calls not found in net.minecraft.world.item.enchantment.ThornsEnchantment.doPostHurt";
                } else {
                    methodNode.instructions.remove(damageItemMain.getPrevious());
                    methodNode.instructions.remove(damageItemMain.getPrevious());
                    methodNode.instructions.remove(damageItemMain.getPrevious().getPrevious().getPrevious());
                    methodNode.instructions.remove(damageItemMain.getPrevious().getPrevious().getPrevious());
                    methodNode.instructions.insert(damageItemMain, damageArmorMain);
                    methodNode.instructions.remove(damageItemMain);
                }
                return methodNode;
            }
        },
        'obscuratorglassfix': {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.block.Block",
                "methodName": ASMAPI.mapMethod("m_152444_"), //"shouldRenderFace",
                "methodDesc": "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z"
            },
            "transformer": function (methodNode) {
                var camouflage = ASMAPI.buildMethodCall(
                    "ic2/core/platform/corehacks/ASMHacks",
                    "applyCamouflage",
                    "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;",
                    ASMAPI.MethodType.STATIC
                );

                var isSideInvisible = null;
                for (var curr = methodNode.instructions.getFirst(); curr !== methodNode.instructions.getLast(); curr = curr.getNext()) {
                    if (curr.getOpcode() === Opcodes.INVOKEVIRTUAL) {
                        if (curr.owner === "net/minecraft/world/level/block/state/BlockState" && curr.name === ASMAPI.mapMethod("m_6104_") /*"skipRendering"*/ && curr.desc === "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z") {
                            isSideInvisible = curr;
                            break;
                        }
                    }
                }

                if (isSideInvisible == null) {
                    throw "net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase.skipRendering not found in net.minecraft.world.level.block.Block.shouldRenderFace";
                } else {
                    var toInsert = new InsnList();
                    toInsert.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    toInsert.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    toInsert.add(new VarInsnNode(Opcodes.ALOAD, 2));
                    toInsert.add(new VarInsnNode(Opcodes.ALOAD, 3));
                    toInsert.add(new VarInsnNode(Opcodes.ALOAD, 4));

                    methodNode.instructions.insertBefore(isSideInvisible.getPrevious().getPrevious(), toInsert);
                    methodNode.instructions.insertBefore(isSideInvisible.getPrevious(), camouflage);
                }

                return methodNode;
            }
        },
        'potiontooltipfix': {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.potion.PotionUtils",
                "methodName": ASMAPI.mapMethod("m_43555_"), // "addPotionTooltip",
                "methodDesc": "(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;F)V"
            },
            "transformer": function (methodNode) {
                var fixEffectList = ASMAPI.buildMethodCall(
                    "ic2/core/platform/corehacks/ASMHacks",
                    "getFixedPotionEffectsFromStack",
                    "(Lnet/minecraft/world/item/ItemStack;F)Ljava/util/List;",
                    ASMAPI.MethodType.STATIC
                );

                var getEffects = null;
                for (var curr = methodNode.instructions.getFirst(); curr !== methodNode.instructions.getLast(); curr = curr.getNext()) {
                    if (curr.getOpcode() === Opcodes.INVOKESTATIC) {
                        if (curr.owner === "net/minecraft/world/item/alchemy/PotionUtils" && curr.name === ASMAPI.mapMethod("m_43547_") /*"getMobEffects"*/ && curr.desc === "(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;") {
                            getEffects = curr;
                            break;
                        }
                    }
                }

                if (getEffects == null) {
                    throw "net.minecraft.world.item.alchemy.PotionUtils.getMobEffects call not found in net.minecraft.world.item.alchemy.PotionUtils.addPotionTooltip";
                } else {
                    methodNode.instructions.insertBefore(getEffects, new VarInsnNode(Opcodes.FLOAD, 2));
                    methodNode.instructions.set(getEffects, fixEffectList);
                }

                return methodNode;
            }
        },
        'tilerenderhook': {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.client.renderer.LevelRenderer",
                "methodName": ASMAPI.mapMethod("m_109599_"), // "renderLevel",
                "methodDesc": "(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V"
            },
            "transformer": function (methodNode) {
                var tileRenderHook = ASMAPI.buildMethodCall(
                    "ic2/core/platform/corehacks/ASMHacks",
                    "onTileRenderFinished",
                    "(Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Z)V",
                    ASMAPI.MethodType.STATIC
                );

                var tileRenderHookTranslucent = ASMAPI.buildMethodCall(
                    "ic2/core/platform/corehacks/ASMHacks",
                    "onTileRenderFinished",
                    "(Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Z)V",
                    ASMAPI.MethodType.STATIC
                );

                var getChestType = null;
                var getWaterMask = null;

                for (var curr = methodNode.instructions.getFirst(); curr !== methodNode.instructions.getLast(); curr = curr.getNext()) {
                    if (curr.getOpcode() === Opcodes.INVOKESTATIC) {
                        if (curr.owner === "net/minecraft/client/renderer/Sheets" && curr.name === ASMAPI.mapMethod("m_110788_") /*"chestSheet"*/ && curr.desc === "()Lnet/minecraft/client/renderer/RenderType;") {
                            getChestType = curr;
                        } else if (curr.owner === "net/minecraft/client/renderer/RenderType" && curr.name === ASMAPI.mapMethod("m_110478_") /*"waterMask"*/ && curr.desc === "()Lnet/minecraft/client/renderer/RenderType;") {
                            getWaterMask = curr;
                        }

                        if (getChestType != null && getWaterMask != null) {
                            break;
                        }
                    }
                }

                if (getChestType == null) {
                    throw "net.minecraft.client.renderer.Sheets.chestSheet call not found in net.minecraft.client.renderer.LevelRenderer.renderLevel";
                } else {
                    var getChestTypeStack = new InsnList();
                    getChestTypeStack.add(new VarInsnNode(Opcodes.ALOAD, 25));
                    getChestTypeStack.add(new InsnNode(Opcodes.ICONST_0)); // translucency = false
                    getChestTypeStack.add(tileRenderHook);

                    methodNode.instructions.insert(getChestType.getNext(), getChestTypeStack);
                }

                if (getWaterMask == null) {
                    throw "net.minecraft.client.renderer.RenderType.waterMask call not found in net.minecraft.client.renderer.LevelRenderer.renderLevel";
                } else {
                    var getWaterMaskStack = new InsnList();
                    getWaterMaskStack.add(new VarInsnNode(Opcodes.ALOAD, 25));
                    getWaterMaskStack.add(new InsnNode(Opcodes.ICONST_1)); // translucency = true
                    getWaterMaskStack.add(tileRenderHookTranslucent);

                    methodNode.instructions.insert(getWaterMask.getNext(), getWaterMaskStack);
                }

                return methodNode;
            }
        },
        'stepassistfix': {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.world.entity.player.Player",
                "methodName": ASMAPI.mapMethod("m_5763_"), //"maybeBackOffFromEdge",
                "methodDesc": "(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/MoverType;)Lnet/minecraft/world/phys/Vec3;"
            },
            "transformer": function (methodNode) {
                for (var curr = methodNode.instructions.getFirst(); curr !== methodNode.instructions.getLast(); curr = curr.getNext()) {
                    if (curr.getOpcode() === Opcodes.INVOKEVIRTUAL) {
                        if (curr.owner === "net/minecraft/world/entity/player/Player" && curr.name === "getStepHeight" && curr.desc === "()F") {
                            var replacement = ASMAPI.buildMethodCall(
                                "ic2/core/platform/corehacks/ASMHacks",
                                "getStepHeight",
                                "(Lnet/minecraft/world/entity/player/Player;)F",
                                ASMAPI.MethodType.STATIC
                            );
                            methodNode.instructions.set(curr, replacement);
                            curr = replacement;
                        }
                    }
                }
                return methodNode;
            }
        }
    }
}