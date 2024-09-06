<?php
$servername = "localhost";
$username = "user";
$password = "user";
$dbname = "gps";
$latitude = $_GET['latitude'] ?? '0';
$longitude = $_GET['longitude'] ?? '0';
$ip = $_GET['ip'] ?? '0';
$timestamp = date('Y-m-d H:i:s');
$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
$sql = "INSERT INTO gps (latitude, longitude, ip, timestamp) VALUES (?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE latitude = VALUES(latitude), longitude = VALUES(longitude), timestamp = VALUES(timestamp)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ssss", $latitude, $longitude, $ip, $timestamp);
$stmt->execute();
echo "OK";
?>