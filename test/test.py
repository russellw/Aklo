import argparse
import os
import re
import subprocess
import time

parser = argparse.ArgumentParser(description="Run test cases")
parser.add_argument("files", nargs="*")
args = parser.parse_args()

here = os.path.dirname(os.path.realpath(__file__))

projectDir = os.path.join(here, "..")
for s in open(os.path.join(projectDir, "pom.xml")).readlines():
    m = re.match(r"\s*<version>(.*)</version>", s)
    if m:
        version = m[1]
        break
else:
    raise Exception()
compiler = os.path.join(
    projectDir, "target", f"aklo-{version}-jar-with-dependencies.jar"
)
cmd = ["java", "-ea", "-jar", compiler]


def search1(p, ss):
    for s in ss:
        if re.search(p, s):
            return 1


def do(file):
    print(file)
    src = [s.strip() for s in open(file).readlines()]

    # check how long the Aklo compiler takes to run
    start = time.time()

    # compile Aklo code
    c = cmd + [file]
    p = subprocess.Popen(
        c,
        stderr=subprocess.PIPE,
    )
    stdout, stderr = p.communicate()
    stderr = str(stderr, "utf-8")

    # are we looking for a compiler error?
    for s in src:
        m = re.match(r";\s*ERR\s+(.*)", s)
        if m:
            if search1(m[1], stderr.splitlines()) and p.returncode:
                return
            raise Exception(stderr)

    # if not, make sure we didn't get one
    if stderr:
        raise Exception(stderr)
    if p.returncode:
        raise Exception(str(p.returncode))

    # check how long the Aklo compiler takes to run
    print(f"{time.time()-start:.3f} seconds")


if args.files:
    for file in args.files:
        do(file)
else:
    for root, dirs, files in os.walk(here):
        for file in files:
            # TODO skip this until functions are working
            if file == "etc.k":
                continue
            ext = os.path.splitext(file)[1]
            if ext == ".k":
                do(os.path.join(root, file))
print("ok")
