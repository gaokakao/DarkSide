<?php
$servername = "localhost";
$username = "user";
$password = "user";
$dbname = "gps";
$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
$latitude = isset($_GET['latitude']) ? floatval($_GET['latitude']) : 0.0;
$longitude = isset($_GET['longitude']) ? floatval($_GET['longitude']) : 0.0;
$user = isset($_GET['user']) ? $_GET['user'] : '';
$sql = "INSERT INTO gps (latitude, longitude, user) VALUES ('$latitude', '$longitude', '$user') ON DUPLICATE KEY UPDATE latitude='$latitude', longitude='$longitude'";
if ($conn->query($sql) !== TRUE) {
    echo "BAD: " . $sql . "  " . $conn->error;
}
$sql = "SELECT user, latitude, longitude FROM gps WHERE user != '$user'";
$result = $conn->query($sql);
$users = [];
if ($result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $userLatitude = floatval($row['latitude']);
        $userLongitude = floatval($row['longitude']);
        $distance = calculateDistance($latitude, $longitude, $userLatitude, $userLongitude);
        $users[] = [
            'user' => $row['user'],
            'distance' => $distance
        ];
    }
}
echo json_encode($users);
$conn->close();
function calculateDistance($latitude1, $longitude1, $latitude2, $longitude2) {
    $earthRadius = 6371000;
    $latitude1 = deg2rad($latitude1);
    $longitude1 = deg2rad($longitude1);
    $latitude2 = deg2rad($latitude2);
    $longitude2 = deg2rad($longitude2);
    $deltaLatitude = $latitude2 - $latitude1;
    $deltaLongitude = $longitude2 - $longitude1;
    $a = sin($deltaLatitude / 2) * sin($deltaLatitude / 2) +
        cos($latitude1) * cos($latitude2) *
        sin($deltaLongitude / 2) * sin($deltaLongitude / 2);
    $c = 2 * atan2(sqrt($a), sqrt(1 - $a));
    return $earthRadius * $c;
}
?>
