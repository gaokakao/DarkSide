<?php
$servername = "localhost";
$username = "user";
$password = "user";
$dbname = "gps";
$latitude = $_GET['latitude'] ?? '0';
$longitude = $_GET['longitude'] ?? '0';
$ip = $_GET['ip'] ?? '0';
$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
$sql = "INSERT INTO gps (latitude, longitude, ip) VALUES (?, ?, ?)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("sss", $latitude, $longitude, $ip);
$stmt->execute();
echo "OK";
?>