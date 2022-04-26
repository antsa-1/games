
Site is currently running @ http://35.217.7.146/portal/index.html with three games, 8-ball (under test), tic-tac-toe and connect four.
<br><br>
### 26.04.2022
New game, eight ball is now available for testing. Play against computer or human, or watch others play.

![tictactoe](./workspace-setup/eight_ball_game.png)
<br>
Some known bugs (or features) exist. UI and backend have own calculations of the table situation. If these calculations do not match then user(s) play against obsolete table. Server calculations are sent as snapshots.They are loaded in case user re-activates the gaming tab (comes back from another tab browser tab/window).
<br>
UI creates turns from the data it receives from the server and then consumes these turns when queue is not blocking.
<br>
Images are from https://opengameart.org/content/8-ball-pool-assets with minor changes.
<br>
Also big help from https://www.youtube.com/watch?v=aXwCrtAo4Wc (part1 and 2)
<br>

![tictactoe](./workspace-setup/architecture_.png)
<br>
### 04.03.2022
New deployment: with read player profile page containing game history. Tag "prod-rel-1.0.3" .

### 19.2.2022
New deployment with player top-lists. Package "prod-rel-1.0.1" .

### 14.2.2022
Initial release
<br>

![tictactoe](./workspace-setup/ConnectFour.png) 

## Further development ideas

UI:
* Add sorting for tables and users, own tab per game for example.
* Make optional notification sound when user enters/creates new table.

Backend:
* New Game?

General:
* Site runs with "http" -> "https" 

Documentation

