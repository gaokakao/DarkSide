<?php
$servername = "localhost";
$username = "user";
$password = "user";
$dbname = "gps";
$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
$latitude = isset($_GET['latitude']) ? $_GET['latitude'] : '';
$longitude = isset($_GET['longitude']) ? $_GET['longitude'] : '';
$user = isset($_GET['user']) ? $_GET['user'] : '';
$sql = "INSERT INTO gps (latitude, longitude, user) VALUES ('$latitude', '$longitude', '$user') ON DUPLICATE KEY UPDATE latitude='$latitude', longitude='$longitude'";
$conn->query($sql);
function calculateDistance($latitude1, $longitude1, $latitude2, $longitude2) {
    $earthRadius = 6371000;
    $latFrom = deg2rad($latitude1);
    $lonFrom = deg2rad($longitude1);
    $latTo = deg2rad($latitude2);
    $lonTo = deg2rad($longitude2);
    $latDelta = $latTo - $latFrom;
    $lonDelta = $lonTo - $lonFrom;
    $angle = 2 * asin(sqrt(pow(sin($latDelta / 2), 2) + cos($latFrom) * cos($latTo) * pow(sin($lonDelta / 2), 2)));
    return $angle * $earthRadius;
}
$sql = "SELECT user, latitude, longitude FROM gps WHERE user != '$user'";
$result = $conn->query($sql);
$distances = [];
if ($result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $distance = calculateDistance($latitude, $longitude, $row['latitude'], $row['longitude']);
        $distances[] = [
            'user' => $row['user'],
            'distance' => round($distance, 3)
        ];
    }
}
$conn->close();
header('Content-Type: application/json');
echo json_encode($distances);