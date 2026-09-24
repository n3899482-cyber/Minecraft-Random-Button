# Random Button test commands

Operators can use these commands while standing in the lobby dimension with their saved Random Button still present:

- `/randombutton test` toggles a server test mode. The button cooldown changes from 30 ticks to 4 ticks, and its pressed animation from 8 ticks to 2 ticks. Run it again to restore the normal timing. Active events still finish before another active event can start.
- `/randombutton random` presses the saved button and selects uniformly from the eligible registered events in the player's current normal, Lucky, or Unlucky pool. A successful random activation consumes one active cube charge.
- `/randombutton event <event>` presses the saved button and chooses a registered event by ID. Use `/randombutton list` to see exact IDs, including `tnt`, `meteor`, `medium_meteor`, `large_meteor`, and `homing_wind`.
- `/randombutton stats` shows total random activations and counts per event in chat. Real button presses and `/randombutton random` are counted; manual `/randombutton event` runs are excluded. Counts are kept in memory and reset when the game process restarts.
- `/randombutton list` shows every registered event ID.

The commands use the saved button position as the normal event origin. Effects that already target the player continue to do so. Commands require operator permission level 2 and do not work from the console or another dimension because there is no valid player button context there.
