# gyro
gyro info from one phone to control the other phone



How to do the test？

Below is the test environment:
PC: which is the develop computer
emu1: which is the AVD emulator to run the client app.
emu2: which is the AVD emulator to run the server app.



     emu1            emu2
   ----------     ----------            
   |        |     |        | listen on 8888
   | client |     | Server |
   |        |     |        |
   ----------      --------- 



          ------------
          |          |
          |          |
          |   PC     |
          |          |
          ------------

You can use the below command to check the devices about the emu1 and emu2
adb devices
example output: 
 emulator-5556   device
 emulator-5554   device

 Here "emulator-5554" is the name of the emu1 which will be used in the following part.
 Here "emulator-5556" is the name of the emu2 which will be used in the following part.


On the PC side:

adb -s emulator-5556 forward tcp:12345 tcp:8888
the above command has the following result:
all the packets send to PC's port 12345 will be forword to emu2's 8888 port.


10.0.2.2 is a special address in the AVD emulator, all the packets send from 10.0.2.2
will send to the host(here is PC), they have the same port.So in client, send a packet to 10.0.2.2:12345 will forward to PC's 12345 port, which will forward to emu2's 8888 port.
 =======================

