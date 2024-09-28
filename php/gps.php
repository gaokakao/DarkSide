<?php
function calculateDistance($lat1, $lon1, $lat2, $lon2)
{
    $earthRadius = 6371000;
    $dLat = ($lat2 - $lat1) * M_PI / 180;
    $dLon = ($lon2 - $lon1) * M_PI / 180;
    $a = sin($dLat / 2) ** 2 + cos($lat1 * M_PI / 180) * cos($lat2 * M_PI / 180) * sin($dLon / 2) ** 2;
    return $earthRadius * (2 * atan2(sqrt($a), sqrt(1 - $a)));
}
$sql = new mysqli(           "localhost", "user", "user", "gps");
if ($sql->connect_error) { die("Connection failed: " . $sql->connect_error);}
$latitude = isset($_GET['latitude']) ? (float)$_GET['latitude'] : 0;
$longitude = isset($_GET['longitude']) ? (float)$_GET['longitude'] : 0;
$user = isset($_GET['user']) ? $_GET['user'] : 'empty';

$sql->query("DELETE FROM gps WHERE user = 'empty'");
$result = $sql->query("SELECT * FROM gps WHERE user='$user'");
if ($result->num_rows > 0) {
    $sql->query("UPDATE gps SET latitude=$latitude, longitude=$longitude WHERE user='$user'");
} else {
    $sql->query("INSERT INTO gps (latitude,longitude,user) VALUES ($latitude,$longitude,'$user')");
}
$sql->query("DELETE FROM gps WHERE user = 'empty'");
$result = $sql->query("SELECT user,latitude,longitude FROM gps");
$users = [];
while ($row = $result->fetch_assoc()) {
    $latitude2 = (float)$row['latitude'];
    $longitude2 = (float)$row['longitude'];
    $row['distance'] = calculateDistance($latitude, $longitude, $latitude2, $longitude2);
    $users[] = $row;
}
echo json_encode($users);
$sql->close();
