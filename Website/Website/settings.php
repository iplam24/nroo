<?php
// settings.php
$ip_sv = "localhost";
$dbname_sv = "ngocrong";
$user_sv = "root";
$pass_sv = "";

if (session_status() == PHP_SESSION_NONE) {
    session_start();
}

require_once __DIR__ . '/connect.php';

date_default_timezone_set('Asia/Ho_Chi_Minh');
?>