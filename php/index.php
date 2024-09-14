<?php
$servername = "localhost";
$username = "user";
$password = "user";
$dbname = "gps";
$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
$latitude = isset($_GET['latitude']) ? $_GET['latitude'] : empty;
$longitude = isset($_GET['longitude']) ? $_GET['longitude'] : empty;
$user = isset($_GET['user']) ? $_GET['user'] : empty;
if ($latitude !== null && $longitude !== null && $user !== null) {
    $latitude = $conn->real_escape_string($latitude);
    $longitude = $conn->real_escape_string($longitude);
    $user = $conn->real_escape_string($user);
    $sql = "INSERT IGNORE INTO gps (latitude, longitude, user) VALUES ('$latitude', '$longitude', '$user')";
    if ($conn->query($sql) === TRUE) {
        echo "New record created successfully! ok";
    } else {
        echo "Error: " . $sql . "<br>" . $conn->error;
    }
} else {
    echo "Missing parameters";
}
$conn->close();
?>
