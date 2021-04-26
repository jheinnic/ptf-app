import sys

import click
from click_option_group import (
    AllOptionGroup,
    RequiredMutuallyExclusiveOptionGroup,
    RequiredAllOptionGroup,
)


@click.group()
def cli(ctx):
    pass


@click.group()
def cli():
    pass


@cli.group()
@click.option("--name", "-n", required=True)
@click.pass_context
def register(ctx: click.Context, name: str):
    if name is None or name == "":
        raise NameError("Name must not be blank or null")
    ctx.obj = name


auth_options = AllOptionGroup()

@register.command()
@click.option(
    "--dir", "-d", type=click.Path(exists=True, dir_okay=True, file_ok=False)
)
def
@cli.command()
@click.option("--host", "-h", type=str, required=True)
@click.option("--port", "-p", type=str, required=True)
@auth_options.option("--user", "-u", type=str)
@auth_options.password_option("--password", "-p")
@click.pass_context
def graph(ctx, uri, user, password):
    from neo4j import GraphDatabase

    if user is None:
        driver = GraphDatabase.driver(uri)
    else:
        driver = GraphDatabase.driver(uri, auth=(user, password))

    ctx.obj = driver


@cli.group()
@click.option("-b")
@click.pass_context
def b(ctx, b):
    print("b", b, ctx.obj, ctx.find_obj(str))


hi.add_command(a)
hi.add_command(b)
lo.add_command(a)
lo.add_command(b)

if __name__ == "__main__":
    cli()
