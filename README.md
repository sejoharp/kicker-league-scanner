# kicker-league-scanner

FIXME: description

## Installation
1. Clone this repo.
2. Put already downloaded matches to the `match-directory-path` directory. Otherwise, the app will start from zero.

## Usage
```shell
.lein.sh run [global-options] command [command options] [arguments...]
```

## commands
The default parameters work for me. 
```shell
NAME:
 kicker-league-scanner - A command-line kicker stats scanner

USAGE:
 kicker-league-scanner [global-options] command [command options] [arguments...]

VERSION:
 1.0.0

COMMANDS:
   download, d          downloads all matches for the given season
   export, e            exports all matches to a given csv file
   upload, u            uploads all matches to nextcloud
   server, s            uploads all matches to nextcloud

GLOBAL OPTIONS:
   -mdp, --match-directory-path S  downloaded-matches  Location of all matches.
   -?, --help
```
## upload options
```shell
NAME:
 kicker-league-scanner upload - uploads all matches to nextcloud

USAGE:
 kicker-league-scanner [upload|u] [command options] [arguments...]

OPTIONS:
   -td, --target-domain S    target domain [$KICKER_TARGET_DOMAIN]
   -tu, --target-user S      target user [$KICKER_TARGET_USER]
   -tp, --target-password S  target password [$KICKER_TARGET_PASSWORD]
   -?, --help
```
## download options
```shell
NAME:
 kicker-league-scanner download - downloads all matches for the given season

USAGE:
 kicker-league-scanner [download|d] [command options] [arguments...]

OPTIONS:
   -s, --season S  2024/25  target season
   -?, --help
```
## server options
```shell
NAME:
 kicker-league-scanner server - uploads all matches to nextcloud

USAGE:
 kicker-league-scanner [server|s] [command options] [arguments...]

OPTIONS:
   -td, --target-domain S    target domain [$KICKER_TARGET_DOMAIN]
   -tu, --target-user S      target user [$KICKER_TARGET_USER]
   -tp, --target-password S  target password [$KICKER_TARGET_PASSWORD]
   -?, --help
```
## export options
```shell
NAME:
 kicker-league-scanner export - exports all matches to a given csv file

USAGE:
 kicker-league-scanner [export|e] [command options] [arguments...]

OPTIONS:
   -tcf, --target-csv-file S  ./all-games-2024-11-10.csv.bz2  Location for the csv file with all games.
   -?, --help
```

## Examples
...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2024 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
