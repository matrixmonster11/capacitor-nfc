export interface NFCPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
