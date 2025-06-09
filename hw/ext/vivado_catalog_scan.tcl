create_project -force /tmp/probe_project

set boards_file [open "/tmp/vivado_boards.txt" w+]
foreach ip [get_boards ] {
    puts $boards_file $ip
}

set parts_file [open "/tmp/vivado_parts.txt" w+]
foreach ip [get_parts ] {
    puts $parts_file $ip
}

set ips_file [open "/tmp/vivado_ips.txt" w+]
foreach ip [get_ipdefs ] {
    puts $ips_file $ip
}