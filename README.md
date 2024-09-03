# TasksLock API README

The 'TasksLock API' is a Springboot module/system meant to help synchronize scheduled
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

To run this module in Embedded Mode, all you have to do is import
this module as dependency to your target module.

### API Mode

This module can be ran as a standalone API incase you want to centralize/segregate 
this functionality.

To run this module in API Mode, you must import this module as a dependency
of your target Springboot app, and set the `tasks-lock.api.enabled` env. var./configuration 
property to `true`.

### API Client Mode

Use this mode in your target module impl. when you have a central
instance of this module in API Mode that you want to consume from.

To run this module in API Mode, you must import this module as a dependency
of your target Springboot app, and set the `tasks-lock.client.enabled` env. var./configuration
property to `true`. You must also set the `tasks-lock.client.api-host` property
to the protocol and hostname of the API Mode module instance(`http://localhost:8080`).