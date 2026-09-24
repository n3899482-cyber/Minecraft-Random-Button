# Recall Sigil

The Recall Sigil is issued when a player joins and the lobby is located. It has a stack limit of one, resists fire, is not consumed on use, and teleports its holder to a safe position near the lobby button with a five second cooldown.

Drop protection runs at the inventory operation itself on both Fabric and NeoForge. Dropping the selected hotbar stack with Q or Ctrl+Q returns an empty stack without removing the sigil. Container clicks that would throw the sigil from a slot or the cursor are canceled. Moving it among player inventory slots, including ordinary pickup and hotbar swaps, remains allowed. Moves from player inventory into external container slots are blocked so closing a container cannot leave the only sigil behind.

Closing a menu while carrying the sigil puts it back into the inventory immediately. The existing Fabric player drop mixin and NeoForge item toss event remain as safety guards for other drop paths. The sigil is removed from death drops and restored during the respawn event. The once per 20 tick inventory check is a fallback for unusual inventory changes or other mods.
