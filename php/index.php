<?php
$servername="localhost";
$username="user";
$password="user";
$dbname="gps";
$conn=new mysqli($servername,$username,$password,$dbname);
if($conn->connect_error){die("Connection failed: ".$conn->connect_error);}
$latitude=$_GET['latitude'];
$longitude=$_GET['longitude'];
$user=$_GET['user'];
$sql="INSERT IGNORE INTO gps (latitude, longitude, user) VALUES ('$latitude', '$longitude', '$user')";
if($conn->query($sql)===TRUE){echo "New record created successfully. ok";}else{echo "Error: ".$sql."<br>".$conn->error;}
$conn->close();
?>