import chisel3._
import dtu.DtuSubsystem
import dtu.DtuSubsystemConfig
import mem.MemoryFactory
import circt.stage.ChiselStage

/**
 * Example design in Chisel.
 * A redesign of the Tiny Tapeout example.
 */
class ChiselTop() extends Module {
  val io = IO(new Bundle {
    val ui_in = Input(UInt(8.W))      // Dedicated inputs
    val uo_out = Output(UInt(8.W))    // Dedicated outputs
    val uio_in = Input(UInt(8.W))     // IOs: Input path
    val uio_out = Output(UInt(8.W))   // IOs: Output path
    val uio_oe = Output(UInt(8.W))    // IOs: Enable path (active high: 0=input, 1=output)
  })

  MemoryFactory.use(mem.ChiselSyncMemory.create)
  val dtu_ss = Module(new DtuSubsystem(DtuSubsystemConfig.default
  .copy(
    romProgramPath = "dtu_ss/leros-asm/didactic_rt.s",
    instructionMemorySize = 1 << 5,
    dataMemorySize = 1 << 5,
    gpioPins = 16
  )))

  dtu_ss.io.apb := DontCare
  dtu_ss.io.ssCtrl := 0.U
  dtu_ss.io.irqEn := false.B

  dtu_ss.io.gpio.in := io.ui_in ## io.uio_in
  io.uo_out := dtu_ss.io.gpio.out(15,8)
  io.uio_oe := dtu_ss.io.gpio.outputEnable
  io.uio_out := dtu_ss.io.gpio.out(7,0)
}

object ChiselTop extends App {
  ChiselStage.emitSystemVerilogFile(new ChiselTop(), Array("--target-dir", "src"), Array("--lowering-options=disallowLocalVariables,disallowPackedArrays"))
}