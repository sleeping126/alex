# ALEX v1.1.2

## Breaking Changes

* Dropped support for IE web driver
* Firefox and Chrome drivers are not supported by default any longer.
  Instead, you have specify the paths to the driver executables.

## Bug Fixes

* Fixed the parsing of JSON paths in various REST actions.

## Features

* New action: Move mouse
* Click action supports double click

# ALEX v1.1 (and hidden ALEX v1.1.1)

## Bug Fixes

* Allow symbol groups to be edited via the frontend again
* Properly close connectors after finished learning
* ALEX v1.1.1 fixed a problem with the fonts.

## Features

* New action: Execute JavaScript
* GoTo action and Call action support Basic HTTP authentication
* Export and import projects
* New REST endpoint: rest/users/batch/{ids} to delete multiple users at once
* Additional visual enhancements

## Other

* Updated frontend and backend dependencies
* Removed requirement to have grunt and grunt-cli installed globally