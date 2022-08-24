# Mining Dimension Setup Script
# by Kzeroko
#
# Spawn Point: 9 11 9
# Structure Block: -17 8 -17 (1 1 1 relative)
# Structure Block Corner: 16 23 16
#
tellraw @p {"text":"Newdim dimension data pack will been re-initialized!", "color":"cyan", "bold":true}

# Force load chunks for the structures
forceload add -64 -64 32 32

# Add Lobby Base
setblock -16 2 -16 minecraft:structure_block{mode: "LOAD", name: "lobby:newdim/dungeoncreate_test"} replace
setblock -16 1 -16 minecraft:redstone_block

# Remove force loaded chunks
forceload remove all
