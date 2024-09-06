<?php
$servername = "localhost";
$username = "user";
$password = "user";
$dbname = "gps";
$latitude = $_GET['latitude'] ?? '0';
$longitude = $_GET['longitude'] ?? '0';
$ip = $_GET['ip'] ?? '0';
$timestamp = date('Y-m-d H:i:s');
// Get city name from OpenCage API
$apiKey = 'YOUR_OPENCAGE_API_KEY'; // Replace with your API key
$geocodeUrl = "https://api.opencagedata.com/geocode/v1/json?q={$latitude}+{$longitude}&key={$apiKey}";
$geocodeResponse = file_get_contents($geocodeUrl);
$geocodeData = json_decode($geocodeResponse, true);
$city = isset($geocodeData['results'][0]['components']['city']) ? $geocodeData['results'][0]['components']['city'] : 'unknown';
// Database connection
$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
// Insert or update data
$sql = "INSERT INTO gps (latitude, longitude, ip, city, timestamp) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE latitude=?, longitude=?, city=?, timestamp=?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ssssssss", $latitude, $longitude, $ip, $city, $timestamp, $latitude, $longitude, $city, $timestamp);
$stmt->execute();
echo json_encode(['status' => 'OK', 'city' => $city]);
$conn->close();
?>