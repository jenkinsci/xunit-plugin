g++ test.cpp

./a.exe --report_format=XML --report_level=detailed
-->produces testresult.xml

./a.exe --output_format=XML --log_level=all
-->produces testlog-testresult.xml

Correct for plugin with only testlog
./a.exe --output_format=XML --log_level=all --report_level=no
-->produces testlog.xml