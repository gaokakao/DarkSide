<?php
$servername = "localhost";
$username = "user";
$password = "user";
$dbname = "gps";
$latitude = $_GET['latitude'] ?? '0';
$longitude = $_GET['longitude'] ?? '0';
$user = $_GET['user'] ?? '0';
$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
// Make sure the columns that uniquely identify a row are properly indexed
$sql = "
    INSERT INTO gps (latitude, longitude, user)
    VALUES (?, ?, ?)
    ON DUPLICATE KEY UPDATE
        latitude = VALUES(latitude),
        longitude = VALUES(longitude),
        user = VALUES(user)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("sss", $latitude, $longitude, $user);
if ($stmt->execute()) {
    echo "OK";
} else {
    echo "Error: " . $stmt->error;
}
$stmt->close();
$conn->close();
?>