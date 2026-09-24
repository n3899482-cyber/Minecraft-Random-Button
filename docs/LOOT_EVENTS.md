# Loot Events

The Random Button now selects thematic loot events through the existing Random Event registry. Each event samples distinct items from its own pool, rolls a count for each stack, and creates one ItemEntity per stack. The selected stacks appear after a 17 tick decorative firework trail and burst above the button. The effect uses particles and sound only, so it causes no explosion damage.

Building Explosion gives 3–7 building materials. Colorful Block Explosion gives 5–8 colored blocks, Wool Explosion gives 6–16 wool colors, and Food Explosion gives 3–6 ordinary foods. Resource Burst uses weighted resource selection, while the rare Valuable Resource Burst favors valuable resources in restrained quantities. Block Shower drops 8–12 small stacks of decorative blocks from above. Potion Burst gives 3–6 distinct ordinary potions, including possible harmful ones. Equipment Burst, Armor Burst, and Weapon & Tool Burst give 1–4 items with strong materials weighted down.

All thematic loot is created near the button with small horizontal impulses. Loot fireworks are visual effects; they never spawn an explosive entity. Ordinary food, armor, and equipment rewards use these firework events instead of older direct inventory rewards. Jackpot and Loot or Death remain distinct special outcomes.
