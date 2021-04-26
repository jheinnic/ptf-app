import click
from os import path as osp


class Repo(object):
    def __init__(self, home=None, debug=False):
        self.home = osp.abspath(home or ".")
        self.debug = debug


pass_repo = click.make_pass_decorator(Repo)


@click.group()
@click.option("--repo-home", envvar="REPO_HOME", default=".repo")
@click.option("--debug/--no-debug", default=False, envvar="REPO_DEBUG")
@click.pass_context
def cli(ctx, repo_home, debug):
    print("cli")
    ctx.obj = Repo(repo_home, debug)


@cli.command()
def a():
    print("a1")


@cli.command()
@pass_repo
def b(repo):
    print("b1: ", repo.home, ",", "t" if repo.debug else "f")


@cli.group(chain=True)
@click.option("-c", type=int)
def c(c):
    print("c1", c)


@c.command()
@click.option("-d", type=int)
def d(d):
    print("d1", d)


@c.command()
@click.option("-f", type=int)
@pass_repo
@click.option("-e", type=int)
def e(repo=None, e=1, f=5):
    print("e1", e, " + ", f, ": ", repo.home, ", ", "t" if repo.debug else "f")


@cli.group(chain=True)
@click.option("-m", type=int)
def m(m):
    print("m1", m)


@m.command()
@click.option("-n", type=int)
def n(n):
    print("n1", n)


@m.command()
@click.option("-p", type=int)
@pass_repo
@click.option("-q", type=int)
def p(repo=None, p=1, q=5):
    print("p1", p, " + ", q, ": ", repo.home, ", ", "t" if repo.debug else "f")


if __name__ == "__main__":
    cli()
