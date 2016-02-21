# nearby-ble-messaging

## Application "Goals"

    1) Discover nearby users of the application.
    2) Be able to have a 1:1 two-way text chat with a nearby user
        bonus) More than one person being able to participate
    3) Pretend it's going to be published on the Play Store
        a) Write tests
        b) Idiomatic code and patterns
    5) Provide APK
    6) As this is an example application that is to emulate what happens with Passport (discovering nearby Beacons).
    7) Kamal also mentioned he's envisioning something like "hey <user> wants to chat with you? Accept, Decline".

As I'm in a different timezone, and I have limited availabilities, I have taken some liberties on the design direction:
  - I'm not going to re-invent the wheel - if there's an existing API that fulfills the requirements, I'll use it.
  - I have elected to use the Nearby Messages API, which allows for detecting of nearby devices over bluetooth, BLE, WiFi network, ultrasound. As this task is BLE focused I will limit using this API with BLE only. This API is available on Android and iOS, so I think it's extremely valid in this setting.
  - I will add code to support Beacon discovery, which is supported within Nearby Messages.
  - I also aim to support multiple-person chat, again doable within Nearby Messages API.
