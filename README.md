# README #

## YAFF - Yet Another Fish Feeder ##

> **Material list:**
>
> - Arduino board (This firmware was tested on UNO board)
> - Servo motor (for example, SG90)
> - Light sensor
> - 10k Ohm resistor
> - Led + 330 Ohm resistor (optional)
> - Small container (Just for fish food)
> - Hot glue

## Schematic ##

![alt tag](Schematic_esquema.png)

## Serial commands ##

You can send some commands to the feeder to get/set some operational parameters or variables

| command |                              parameter                              |                                     description                                    |
|:-------:|:-------------------------------------------------------------------:|:----------------------------------------------------------------------------------:|
|    FE   |                                 N/A                                 | Force the feeding cycle                                                             |
|   FTX   |            X: Feed times in a cycle (default 2)                     | Set feed times to new value                                                        |
| FIXXXXX |                XXXXX: feed interval (default 28800s)                | Set feed interval to new value                                                     |
|   FNX   |            X: 0 disabled / 1 enabled                                | Enable/Disable feeding at night |
|    SD   |                                 N/A                                 | Save all parameters into EEPROM                                                    |
|    RD   |                                 N/A                                 | Reset the feeder to default values and delete EEPROM                                   |
|    ST   |                                 N/A                                 | Show the feeder status  (status, version, feed interval,feed times, last feed time, servo vars, light sensor vars) |
|    SS    |                                N/A                                 | Turn on/off status light                 
|  CLSPIN  |            X: analog pin for light sensor (default 2)              | Change light sensor pin                                                                   |
|  CLTHR  |          XXXX: light sensor threshold (default 1023)                | Change light sensor threshold                                                                   |
|  CSPIN  |            X: pin attached to servo (default 9)                     | Change servo pin                                                                   |
|  CSPOS  |                 XXX: Servo start position (0 to 180)                | Change servo start position                                                        |
|  CMPOS  | XXX: Servo mid position (0 to 180) and  greater than start position | Change servo mid position                                                          |
|  CEPOS  | XXX: Servo end position (0 to 180) and  greater than mid position   | Change servo end position                                                          |
|  CLDLY  | XXXXX: Change servo long delay (default 1000ms)                     | Change servo long delay                                                            |
|  CSDLY  | XXXXX: Change servo short delay (default 150ms)                     | Change servo short delay                                                           |
|    VE   |                                 N/A                                 | Show firmware version                                                              |

## Feeder status ##

> Feeder has 3 states:
>
> - *STATE_STARTING (0):* indicates the feeder is setting up or was reset, during this state the feeder will ignore all commands received.
> - *STATE_FEEDING (2):* indicates the feeder is busy in a middle of the feeding cycle, during this state, the feeder will return feeding cycle progress if a command if received.
> - *STATE_NORMAL (1):* indicates the feeder is not busy and its interface could receive any command.
