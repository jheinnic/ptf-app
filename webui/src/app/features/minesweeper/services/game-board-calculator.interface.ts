export class GameCellConverterService {
  public toCoordinatePair(cellIndex: number, xSize: number, ySize: number): {xCell: number, yCell: number}
  {
    const yCoordinate = cellIndex % ySize;
    const xCoordinate = (cellIndex - yCoordinate) / ySize;
    return {xCell: xCoordinate, yCell: yCoordinate};
  }

  public toCellIndex(xCell: number, yCell: number, xSize: number, ySize: number): number
  {
    return (xCell * ySize) + yCell;
  }
}
