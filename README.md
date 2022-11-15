![https://i.imgur.com/ApkCPZ3.png](https://i.imgur.com/ApkCPZ3.png)

[![Discord](https://img.shields.io/discord/797713290545332235?logo=discord&style=flat-square)](https://discord.gg/9PAesuHFnp)
[![GitHub license](https://img.shields.io/github/license/samolego/ClientStorage?style=flat-square)](https://github.com/samolego/ClientStorage/blob/master/LICENSE)
[![Client environment](https://img.shields.io/badge/Environment-client-green?style=flat-square)](https://github.com/samolego/ClientStorage)
[![Singleplayer environment](https://img.shields.io/badge/Environment-singleplayer-yellow?style=flat-square)](https://github.com/samolego/ClientStorage)

*Highly inspired by AE2 / Refined Storage ... but clientside!*

A **clientside** mod (yes you can use it on any server,
though anticheats might interfer) to never search
for an item again!

Having a chestmonster? You'll be able to deal with it
easily!

### Features
* toggle with a keybind
* see all items in chests that are in your reach proximity
* any resourcepack support (gui is made out of creative textures)
* pick up items seamlessly
* search for items via built-in searchbox
    * `#` for tags (e. g. `#protection` - will search for any items having `protection` tag) 
    * `@` for modded items (e. g. `@simplevillagers` - will search for items from `simplevillagers` mod)
* autodetect server type and modify needed packet delays (to some extent)

## Screenshots

| Zoomed      | Whole inventory|
| ----------- | -------------: |
| <img src="https://user-images.githubusercontent.com/34912839/197386601-34e257da-a8f3-4c1b-8def-3e794f7b925a.png"> <img src="https://user-images.githubusercontent.com/34912839/197386743-156db4e8-f9a0-44ca-86c2-6542838074de.png"> | <img src="https://user-images.githubusercontent.com/34912839/197386793-1d4da9ad-b6a9-462c-951a-4bdbb405fa75.png"> |


## Showcase

https://user-images.githubusercontent.com/34912839/195037919-678bdfc7-b8c9-474d-b397-170821cf2781.mp4

https://user-images.githubusercontent.com/34912839/201891412-4ac7cb81-e3fb-4d08-a965-8959d7c5e98c.mp4

## Current problems

* detecting server is done via `brand` field from `CustomPayload` - this could be improved
* items sometimes glitch out
* items are "shift-clicked"

## Server owners
* want to limit some mod features (e.g. through wall container discovery?)
    * you can install mod on fabric or bukkit-type servers and sync the config to the client!

## Contributions

There's so much more that can be done! If you want to help,
feel free to fork the repo and submit a pull request! Thanks :wink:.

