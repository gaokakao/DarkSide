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

if ($conn->query($sql) === TRUE) {
    echo "Record inserted or updated successfully! ok";
} else {
    echo "Error: " . $sql . "<br>" . $conn->error;
}

$conn->close();
