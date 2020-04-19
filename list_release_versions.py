from github import Github
import re
import urllib
import sys
import multiprocessing

if len(sys.argv) < 2:
    print "Invalid arguments. (" + str(sys.argv) + ")"
    exit(-1)

if sys.argv[1] != "" :
    g = Github(sys.argv[1])
else:
    g = Github()

repo = g.get_repo(sys.argv[2])

rels = repo.get_releases()

PATTERN = re.compile("^v(.*)")

for r in rels:
    search = PATTERN.search(r.tag_name)
    if search :
        version = search.group(1)
        print version

