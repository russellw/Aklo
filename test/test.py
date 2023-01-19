import argparse
import os
import re
import subprocess
import time

parser = argparse.ArgumentParser(description="Run test cases")
parser.add_argument("-v", "--verbose", action="count", help="increase output verbosity")
parser.add_argument("files", nargs="*")
args = parser.parse_args()

verbose = 0
if args.verbose:
    verbose = args.verbose

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


def search1(p, ss):
    for s in ss:
        if re.search(p, s):
            return 1


def do(file):
    if verbose >= 1:
        print(file)
    src = [s.strip() for s in open(file).readlines()]

    # check how long the Aklo compiler takes to run
    start = time.time()

    # compile Aklo code
    p = subprocess.Popen(
        ("java", "-ea", "-jar", compiler, file),
        stderr=subprocess.PIPE,
    )
    stdout, stderr = p.communicate()
    stderr = str(stderr, "utf-8")

    # are we looking for a compiler error?
    for s in src:
        m = re.match(r";\s*ERR\s+(.*)", s)
        if m:
            if search1(m[1], stderr.splitlines()) and p.returncode:
                if verbose >= 2:
                    print(stderr, end="")
                return
            raise Exception(stderr)

    # if not, make sure we didn't get one
    if stderr:
        raise Exception(stderr)
    if p.returncode:
        raise Exception(str(p.returncode))

    # check how long the Aklo compiler takes to run
    if verbose >= 2:
        print(f"{time.time()-start:.3f} seconds")

    # run the program
    p = subprocess.Popen(
        ("java", "-cp", ".;" + compiler, "-ea", "a"),
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    stdout, stderr = p.communicate()
    stdout = str(stdout, "utf-8")
    stderr = str(stderr, "utf-8")
    if verbose >= 2:
        print(stdout, end="")

    # not expecting a runtime error
    if stderr:
        raise Exception(stderr)
    if p.returncode:
        raise Exception(str(p.returncode))

    # are we looking for particular output?
    for i in range(len(src)):
        if src[i] == "{" and src[i + 1] == "OUT":
            r = ""
            for s in src[i + 2 :]:
                if s == "}":
                    break
                r += s + "\n"
            stdout = stdout.replace("\r", "")
            if stdout == r:
                return
            print(repr(r))
            print(repr(stdout))
            raise Exception()


if args.files:
    for file in args.files:
        do(file)
else:
    for root, dirs, files in os.walk(here):
        for file in files:
            ext = os.path.splitext(file)[1]
            if ext == ".k":
                do(os.path.join(root, file))
print("ok")
