<?php
// connect.php
$ip_sv = "localhost";
$dbname_sv = "ngocrong";
$user_sv = "root";
$pass_sv = "";

$thesieure_url = 'https://thesieure.com/chargingws/v2';
$thesieure_partner_id = '97860629743';
$thesieure_partner_key = 'c631d6023de6ffc308f0d01078dcde85';


$conn = new mysqli($ip_sv, $user_sv, $pass_sv, $dbname_sv);

if ($conn->connect_error) {
    die("Lỗi kết nối database: " . $conn->connect_error);
}

$conn->set_charset("utf8mb4");

date_default_timezone_set('Asia/Ho_Chi_Minh');
?>