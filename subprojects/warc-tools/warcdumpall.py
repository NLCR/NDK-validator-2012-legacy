#!/usr/bin/env python
"""warcdumpall - dump warcs into files into destination folder"""

import os
import sys

import sys
import os.path

from optparse import OptionParser

from hanzo.warctools import ArchiveRecord, WarcRecord

parser = OptionParser(usage="%prog [options] warc warc warc")

parser.add_option("-L", "--log-level", dest="log_level")
parser.add_option("-o", "--output-directory", dest="output_directory")

parser.set_defaults(output_directory=None, limit=None, log_level="info")

def main(argv):
    (options, input_files) = parser.parse_args()
    out = sys.stdout
    
    if not options.output_directory:
        parser.error("option -o is mandatory")
        
    if not os.path.isdir(options.output_directory):
        os.makedirs(options.output_directory)
    
    if len(input_files) < 1:
        parser.error("list of warc files is mandatory")
        
    else:
        for name in input_files:
            fh = ArchiveRecord.open_archive(name, gzip="auto")
            dump_archive(fh,name,options.output_directory)

            fh.close()

    return 0        

def dump_archive(fh, name, directory, offsets=True):
    for (offset, record, errors) in fh.read_records(limit=None, offsets=offsets):
        if record:
            #print "archive record at %s:%s"%(name,offset)
            record.dump_to_file(directory, onlyResponse=True)
        elif errors:
            print "warc errors at %s:%d"%(name, offset if offset else 0)
            for e in errors:
                print '\t', e
        else:
            print 'note: no errors encountered in tail of file'


if __name__ == '__main__':
    sys.exit(main(sys.argv))



