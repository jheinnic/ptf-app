export interface IGameCellConverterService
{
  toCoordinatePair(cellIndex: number, xSize: number, ySize: number): { xCell: number, yCell: number }

  toCellIndex(xCell: number, yCell: number, xSize: number, ySize: number): number;
}
