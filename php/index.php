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
// Make sure the columns that uniquely identify a row are properly indexed
$sql = "
    INSERT INTO gps (latitude, longitude, ip)
    VALUES (?, ?, ?)
    ON DUPLICATE KEY UPDATE
        latitude = VALUES(latitude),
        longitude = VALUES(longitude),
        ip = VALUES(ip)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("sss", $latitude, $longitude, $ip);
if ($stmt->execute()) {
    echo "OK";
} else {
    echo "Error: " . $stmt->error;
}
$stmt->close();
$conn->close();
?>