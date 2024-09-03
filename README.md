# TasksLock API README

The 'TasksLock API' is a Springboot module meant to help synchronize scheduled
methods/work in a HA environment. It is useful for enforcing a limit on 
jobs that should only be run once at the same time.

## Module Modes

The 'TasksLock API' has three modes:

- Embedded Mode
- API Mode
- API Client Mode

### Embedded Mode

Embedded mode is meant for cases when you want your target Springboot
application to handle all the database configuration.

### API Mode

This module can be ran as a standalone API incase you want to centralize/segregate 
this functionality.

### API Client Mode

Use this mode in your target module impl. when you have a central
instance of this module in API Mode that you want to consume from.