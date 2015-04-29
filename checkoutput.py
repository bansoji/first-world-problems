#!/usr/bin/python

import sys
import difflib

def main():
    if len(sys.argv) != 3:
        print >>sys.stderr, "Usage: %s <expectedOutput.csv> <output.csv>" % sys.argv[0]
        sys.exit(1)


    file_exp = open(sys.argv[1], 'r')
    file_in = open(sys.argv[2], 'r')

    diff = difflib.ndiff(file_exp.readlines(), file_in.readlines())
    differences = ''.join(x for x in diff if x.startswith('- ') or x.startswith('+ '))
    print "-: Missing compared to expected\n+: Extra compared to expected"
    print differences


if __name__ == "__main__":
    main()
