import sys

import click
from os import path as osp


def on_hilo(ctx, param, value):
    print("on_hilo", param, value, ctx.params, ctx.obj)

    if value is None:
        if ctx.obj is None:
            ctx.fail("Must set either --hi or --lo")
    elif ctx.obj is not None:
        ctx.fail("Cannot set both --hi and --lo")
    elif param.name == "lo":
        if ctx.params["transformation"] is None:
            ctx.params.update({"transformation": "lower"})
        elif ctx.params["transformation"] == "upper":
            ctx.fail("Cannot set --lo with --upper")
    elif param.name == "hi":
        if ctx.params["transformation"] is None:
            ctx.params.update({"transformation": "upper"})
        elif ctx.params["transformation"] == "lower":
            ctx.fail("Cannot set --hi with --lower")
    else:
        ctx.fail("Unknown param name", param.name)

    ctx.obj = param.name
    return value


@click.command()
@click.option("--lower", "transformation", flag_value="lower", is_eager=True)
@click.option("--upper", "transformation", flag_value="upper", is_eager=True)
@click.option("--hi", type=int, required=False, callback=on_hilo)
@click.option("--lo", type=int, required=False, callback=on_hilo)
@click.pass_context
def cli(ctx, transformation, hi, lo):
    click.echo(getattr(sys.platform, transformation)())


if __name__ == "__main__":
    cli()
