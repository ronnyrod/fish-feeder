# README #

## Just another automatic fish feeder based on Arduino ##

> **List of material:**
>
> - Arduino board (This firmware was tested on UNO board)
> - Servo motor (for example, SG90)
> - Small container (Just for fish food)
> - Hot glue

## Serial commands ##

You can send some commands to feeder to get/set some operational parameters or variables

> ### Get firmware version ###

 > > *command:* **VE**

 > > *output:* Returns a string indicating firmware version. Example: ***VE1***

> ### Get fish feeder status ###

 > > *command:* **ST**

 > > *output:* Returns a string indicating feeder status as follow: ST[0: Starting|1: Normal|2: Feeding];FI[feed interval in seconds, default 28800s];LF[time in milliseconds since last feeding operation]

> ### Change feeding interval ###

 > > *command:* **FIXXXX** (XXXXX five digit number indicating number of seconds between each feeding operation (default: 28800s)

 > > *output:* Returns a string indicating number of seconds between each feeding operation, for example: **FI14400**

> ### Force manual feeding ###

 > > *command:* **FEX** (X can be empty (default value) or a number between 1 and 9 indicating turn count of a feeding operation

 > > *output:* Returns a string indicating turn count of a feeding operation, for example: **FE2** (A feeding operation executes two turns)