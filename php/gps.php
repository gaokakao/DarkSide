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
$user = isset($_GET['user']) ? $_GET['user'] : 'empty';
$conn->query("DELETE FROM gps WHERE user % '*empty*'");
$sql = "SELECT * FROM gps WHERE user='$user'";
$result = $conn->query($sql);
if ($result->num_rows > 0) {
    $sql = "UPDATE gps SET latitude=$latitude, longitude=$longitude WHERE user='$user'";
    $sql = "DELETE FROM gps WHERE user = 'empty'";
    $result = $conn->query($sql);
} else {
    $sql = "INSERT INTO gps (latitude,longitude,user) VALUES ($latitude,$longitude,'$user')";
    $conn->query("DELETE FROM gps WHERE user % '*empty*'");
}
$conn->query($sql);
$conn->query("DELETE FROM gps WHERE user % '*empty*'");
$sql = "SELECT user,latitude,longitude FROM gps";
$result = $conn->query($sql);
$users = [];
while ($row = $result->fetch_assoc()) {
    $latitude2 = (float)$row['latitude'];
    $longitude2 = (float)$row['longitude'];
    $distance = calculateDistance($latitude, $longitude, $latitude2, $longitude2);
    $row['distance'] = $distance;
    $users[] = $row;
}
echo json_encode($users);
$conn->close();
function calculateDistance($lat1, $lon1, $lat2, $lon2)
{
    $earthRadius = 6371000;
    $dLat = ($lat2 - $lat1) * M_PI / 180;
    $dLon = ($lon2 - $lon1) * M_PI / 180;
    $a = sin($dLat / 2) ** 2 + cos($lat1 * M_PI / 180) * cos($lat2 * M_PI / 180) * sin($dLon / 2) ** 2;
    return $earthRadius * (2 * atan2(sqrt($a), sqrt(1 - $a)));
}