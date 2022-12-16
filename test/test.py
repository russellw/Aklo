import argparse
import os
import re
import subprocess


def search1(p, ss):
    for s in ss:
        if re.search(p, s):
            return 1


def do(file):
    print(file)
    src = [s.strip() for s in open(file).readlines()]

    # compile Aklo code
    cmd = "java", "-cp", r"C:\aklo\boot", "-ea", "a.java", file  # , r"C:\aklo\aklo"
    p = subprocess.Popen(
        cmd,
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

    # compile C++ code
    subprocess.check_call("cl /nologo a.cc")

    # run the program
    cmd = "a"
    p = subprocess.Popen(
        cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    stdout, stderr = p.communicate()
    stdout = str(stdout, "utf-8")
    stderr = str(stderr, "utf-8")
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


parser = argparse.ArgumentParser(description="Run test cases")
parser.add_argument("files", nargs="*")
args = parser.parse_args()
if args.files:
    for file in args.files:
        do(file)
else:
    here = os.path.dirname(os.path.realpath(__file__))
    for root, dirs, files in os.walk(here):
        for file in files:
            ext = os.path.splitext(file)[1]
            if ext == ".k":
                do(os.path.join(root, file))
print("ok")
