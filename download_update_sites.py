from github import Github
import re
import urllib
import sys
import multiprocessing

if len(sys.argv) != 2:
    print("Invalid arguments. (" + str(sys.argv) + "))
    exit(-1)

g = Github(sys.argv[1])

repo = g.get_repo("sakerbuild/saker.build.ide.eclipse")

rels = repo.get_releases()

PATTERN = re.compile("^update-site-v(.*)")

def downloadSite(arg):
    asset = arg['asset']
    version = arg['version']
    print("Downloading: " + version + " from " + asset.browser_download_url)
    urllib.urlretrieve(asset.browser_download_url, "site-v" + version + ".zip")

pool = multiprocessing.Pool(16);

dl = []

for r in rels:
    search = PATTERN.search(r.tag_name)
    if search :
        version = search.group(1)
        print(r.tag_name)
        for asset in r.get_assets(): 
            if asset.name == 'site.zip':
                print(asset.browser_download_url)
                print(asset.url)
                dl.append({ 'asset': asset, 'version': version })
                

pool.map(downloadSite, dl)
