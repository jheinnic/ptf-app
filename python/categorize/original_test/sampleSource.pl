#!/usr/local/bin/perl

open(IN, "images/collected_log.dat");
open(OUT, ">lane_model_test/test_src.dat");

my $counter = 0;
while(<IN>) {
    if ($counter == 13) {
        print OUT $_;
        $counter = 0;
    } else {
        $counter = $counter + 1;
    }
}
close(OUT);
close(IN);
