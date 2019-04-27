# ItemMail
A plugin of sharing items in imyvm server, prepared for minecraft 1.13.

It is designed for the new minecraft server [future] in imyvm minecraft circle.
## Introduce:
In the [future] imyvm server, you can send anything to the [MainWorld] which you acquired. 
## Commands for player
* /itemmail send [player]  - Send the item in your hand to yourself or others.
* /itemmail get            - Get the items from your mailbox, the mailbox size is suggested as 36.
* /itemmail sendtotal      - Send all items in your inventory to yourself.
* /itemmail open     - open your mailbox
* /itemmail confirm   - confirm send items
## Commands for ops
* /itemmail open [player]  - Open the player's mailbox, but you can't change it since now.
* /itemmail create [player] - Create mailbox for player, which is not suggested to use, since the account will create automatically when player login in the server firstly.
## Permissions
* ItemMail.player.*
  - ItemMail.send.self
  - ItemMail.get
  - ItemMail.send.total
  - ItemMail.send.others
  - ItemMail.send.confirm
  - ItemMail.open
* ItemMail.admin.*
  - ItemMail.open.others
  - ItemMail.create
## Usage
Stop your server and drop the plugin in your plugin folder, start your server and modify the config, restart your server and enjoy it!
