<?php
$servername = "localhost";
$username = "user";
$password = "user";
$dbname = "gps";
$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
$latitude = isset($_GET['latitude']) ? (float)$_GET['latitude'] : 0;
$longitude = isset($_GET['longitude']) ? (float)$_GET['longitude'] : 0;
$user = isset($_GET['user']) ? $_GET['user'] : '';
$sql = "INSERT INTO gps (latitude, longitude, user) VALUES ('$latitude', '$longitude', '$user') ON DUPLICATE KEY UPDATE latitude='$latitude', longitude='$longitude'";
if ($conn->query($sql) === TRUE) {
    $sql = "SELECT user, latitude, longitude FROM gps";
    $result = $conn->query($sql);
    $users = [];
    while ($row = $result->fetch_assoc()) {
        $latitude1 = $latitude;
        $longitude1 = $longitude;
        $latitude2 = (float)$row['latitude'];
        $longitude2 = (float)$row['longitude'];
        $distance = calculateDistance($latitude1, $longitude1, $latitude2, $longitude2);
        $row['distance'] = $distance;
        $users[] = $row;
    }
    echo json_encode($users);
} else {
    echo "BAD: " . $sql . " " . $conn->error;
}
$conn->close();
function calculateDistance($latitude1, $longitude1, $latitude2, $longitude2)
{
    $earthRadius = 6371000;
    $dLat = deg2rad($latitude2 - $latitude1);
    $dLon = deg2rad($longitude2 - $longitude1);
    $a = sin($dLat / 2) * sin($dLat / 2) +
        cos(deg2rad($latitude1)) * cos(deg2rad($latitude2)) *
        sin($dLon / 2) * sin($dLon / 2);
    $c = 2 * atan2(sqrt($a), sqrt(1 - $a));
    $distance = $earthRadius * $c;
    return $distance;
}