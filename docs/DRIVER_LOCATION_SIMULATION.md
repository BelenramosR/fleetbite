# Driver location simulation

## Restaurant pickup point

FleetBite uses the Google Maps pin for Ibuki Sushi Bar:

- Address: Av. Manuel Olguín 561, Santiago de Surco 15023
- Latitude: `-12.0919738`
- Longitude: `-76.9737017`

The defaults can be overridden without changing code:

```powershell
$env:RESTAURANT_LATITUDE="-12.0919738"
$env:RESTAURANT_LONGITUDE="-76.9737017"
```

When a vehicle is assigned to a driver, the driver's current location is initialized at
this pickup point. Location belongs to the driver, not to the vehicle entity.

## Auto-assignment behavior

Auto-assignment compares each available driver's current location with the restaurant.
The driver with the shortest Haversine distance to the pickup point is selected. The
customer delivery location is retained for the later restaurant-to-customer leg.

## Presentation simulation

Prerequisites:

1. Start the backend.
2. Log in and copy an ADMIN or DISPATCHER access token.
3. Create a DRIVER user, add their phone, create a vehicle and assign it to the driver.
4. Put the driver online.
5. Obtain the driver UUID and choose destination coordinates.

Run from the repository root:

```powershell
.\backend\scripts\simulate-driver-movement.ps1 `
  -Token "ACCESS_TOKEN" `
  -DriverId "DRIVER_UUID" `
  -DestinationLatitude -12.1000000 `
  -DestinationLongitude -76.9900000 `
  -Steps 20 `
  -IntervalSeconds 2
```

The script starts at the restaurant and calls
`PATCH /api/v1/drivers/{id}/location` once per step. Any frontend polling or subscribing
to driver locations can render the movement.

This is a deterministic presentation helper, not a road-routing engine: points follow a
straight geographic interpolation and do not account for streets, traffic or travel time.
