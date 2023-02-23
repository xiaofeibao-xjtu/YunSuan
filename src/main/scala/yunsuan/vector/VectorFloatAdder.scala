package yunsuan.vector

import chisel3._
import chisel3.util._
import yunsuan.{OpType, VectorElementFormat}

/**
 * f16/f32/f64 vector float add/sub/min/max/compare
 * carry or borrow output
 * support fadd/fsub/fmin/fmax/fcompare instruction
 **/
class VectorFloatAdder() extends Module {
  val exponentWidth = 11
  val significandWidth = 53
  val floatWidth = exponentWidth + significandWidth
  val io = IO(new Bundle() {
    val fp_a, fp_b    = Input (UInt(floatWidth.W))
    val is_vec        = Input (Bool())
    val round_mode    = Input (UInt(3.W))
    val fp_format     = Input (VectorElementFormat()) // result format b01->fp16,b10->fp32,b11->fp64
    val opb_widening  = Input (Bool())    // ture -> opb widening
    val res_widening  = Input (Bool())    // true -> widening operation
    val op_code       = Input (OpType())  // 0000Add 0001Min 0010Max 0011FEQ 0100FNE 0101FLT 0110FLE 0111FGT 1000FGE 1001Sub

    val fp_f64_result = Output(UInt(floatWidth.W))
    val fp_f32_result = Output(UInt(floatWidth.W))
    val fp_f16_result = Output(UInt(floatWidth.W))
    val fflags        = Output(UInt(20.W))
  })
  // TODO change fp_format is_vec logic
  // assert(io.fp_format=/=0.U) // TODO: add valid to enable assert
  val fp_format = Cat(io.fp_format===3.U,io.fp_format(1))
  val is_sub = io.op_code(3) & io.op_code(0)

  val hasMinMaxCompare = true
  val is_add = io.op_code(3,0) === 0.U
  val is_min = io.op_code(3,0) === 1.U
  val is_max = io.op_code(3,0) === 2.U
  val is_feq = io.op_code(3,0) === 3.U
  val is_fne = io.op_code(3,0) === 4.U
  val is_flt = io.op_code(3,0) === 5.U
  val is_fle = io.op_code(3,0) === 6.U
  val is_fgt = io.op_code(3,0) === 7.U
  val is_fge = io.op_code(3,0) === 8.U
  val U_F32_Mixed_0 = Module(new FloatAdderF32WidenF16MixedPipeline(is_print = false,hasMinMaxCompare = hasMinMaxCompare))
  U_F32_Mixed_0.io.fp_a := io.fp_a(31,0)
  U_F32_Mixed_0.io.fp_b := io.fp_b(31,0)
  U_F32_Mixed_0.io.is_sub := is_sub
  U_F32_Mixed_0.io.round_mode   := io.round_mode
  U_F32_Mixed_0.io.fp_format    := fp_format
  U_F32_Mixed_0.io.res_widening := io.res_widening
  U_F32_Mixed_0.io.opb_widening := io.opb_widening
  U_F32_Mixed_0.io.op_code      := io.op_code(3,0)
  val U_F32_0_result = U_F32_Mixed_0.io.fp_c
  val U_F32_0_fflags = U_F32_Mixed_0.io.fflags
  val U_F16_0_result = U_F32_Mixed_0.io.fp_c(15,0)
  val U_F16_0_fflags = U_F32_Mixed_0.io.fflags

  val U_F32_Mixed_1 = Module(new FloatAdderF32WidenF16MixedPipeline(is_print = false,hasMinMaxCompare = hasMinMaxCompare))
  U_F32_Mixed_1.io.fp_a := io.fp_a(63,32)
  U_F32_Mixed_1.io.fp_b := io.fp_b(63,32)
  U_F32_Mixed_1.io.is_sub := is_sub
  U_F32_Mixed_1.io.round_mode   := io.round_mode
  U_F32_Mixed_1.io.fp_format    := fp_format
  U_F32_Mixed_1.io.res_widening := io.res_widening
  U_F32_Mixed_1.io.opb_widening := io.opb_widening
  U_F32_Mixed_1.io.op_code      := io.op_code(3,0)
  val U_F32_1_result = U_F32_Mixed_1.io.fp_c
  val U_F32_1_fflags = U_F32_Mixed_1.io.fflags
  val U_F16_2_result = U_F32_Mixed_1.io.fp_c(15,0)
  val U_F16_2_fflags = U_F32_Mixed_1.io.fflags

  val U_F64_Widen_0 = Module(new FloatAdderF64WidenPipeline(is_print = false,hasMinMaxCompare = hasMinMaxCompare))
  U_F64_Widen_0.io.fp_a := io.fp_a
  U_F64_Widen_0.io.fp_b := io.fp_b
  U_F64_Widen_0.io.is_sub := is_sub
  U_F64_Widen_0.io.round_mode := io.round_mode
  U_F64_Widen_0.io.res_widening := io.res_widening
  U_F64_Widen_0.io.opb_widening := io.opb_widening
  U_F64_Widen_0.io.op_code      := io.op_code(3,0)
  val U_F64_Widen_0_result = U_F64_Widen_0.io.fp_c
  val U_F64_Widen_0_fflags = U_F64_Widen_0.io.fflags

  val U_F16_1 = Module(new FloatAdderF16Pipeline(is_print = false,hasMinMaxCompare = hasMinMaxCompare))
  U_F16_1.io.fp_a := io.fp_a(31,16)
  U_F16_1.io.fp_b := io.fp_b(31,16)
  U_F16_1.io.is_sub := is_sub
  U_F16_1.io.round_mode := io.round_mode
  U_F16_1.io.op_code    := io.op_code(3,0)
  val U_F16_1_result = U_F16_1.io.fp_c
  val U_F16_1_fflags = U_F16_1.io.fflags

  val U_F16_3 = Module(new FloatAdderF16Pipeline(is_print = false,hasMinMaxCompare = hasMinMaxCompare))
  U_F16_3.io.fp_a := io.fp_a(63,48)
  U_F16_3.io.fp_b := io.fp_b(63,48)
  U_F16_3.io.is_sub := is_sub
  U_F16_3.io.round_mode := io.round_mode
  U_F16_3.io.op_code    := io.op_code(3,0)
  val U_F16_3_result = U_F16_3.io.fp_c
  val U_F16_3_fflags = U_F16_3.io.fflags

  val res_is_f16 = io.fp_format === 0.U
  val res_is_f32 = io.fp_format === 1.U
  val res_is_f64 = io.fp_format === 2.U
  io.fp_f64_result := U_F64_Widen_0_result
  io.fp_f32_result := Cat(U_F32_1_result,U_F32_0_result)
  io.fp_f16_result := Cat(U_F16_3_result,U_F16_2_result,U_F16_1_result,U_F16_0_result)
  io.fflags := Cat(
    U_F16_3_fflags,
    U_F16_2_fflags,
    Mux(RegNext(res_is_f32),U_F32_1_fflags,U_F16_1_fflags),
    Mux(
      RegNext(res_is_f64),
      U_F64_Widen_0_fflags,
      Mux(RegNext(res_is_f32),U_F32_0_fflags,U_F16_0_fflags)
    )
  )
//    Mux(
//    RegNext(res_is_f64),
//    U_F64_Widen_0_fflags,
//    Mux(
//      RegNext(res_is_f32),
//      U_F32_0_fflags | (Fill(5,RegNext(io.is_vec)) & U_F32_1_fflags),
//      U_F16_0_fflags | (Fill(5,RegNext(io.is_vec)) & (U_F16_1_fflags | U_F16_2_fflags | U_F16_3_fflags))
//    )
//  )
}

private[vector] class FloatAdderF32WidenF16MixedPipeline(val is_print:Boolean = false,val hasMinMaxCompare:Boolean = false) extends Module {
  val exponentWidth = 8
  val significandWidth = 24
  val floatWidth = exponentWidth + significandWidth
  val io = IO(new Bundle() {
    val fp_a, fp_b   = Input (UInt(floatWidth.W))
    val fp_c         = Output(UInt(floatWidth.W))
    val is_sub       = Input (Bool())
    val round_mode   = Input (UInt(3.W))
    val fflags       = Output(UInt(5.W))
    val fp_format    = Input (UInt(2.W))
    val opb_widening = Input (Bool())
    val res_widening = Input (Bool())
    val op_code = if (hasMinMaxCompare) Input(UInt(4.W)) else Input(UInt(0.W))
  })
  val res_is_f32 = io.fp_format(0).asBool
  val fp_a_16as32 = Cat(io.fp_a(15), Cat(0.U(3.W),io.fp_a(14,10)), Cat(io.fp_a(9,0),0.U(13.W)))
  val fp_b_16as32 = Cat(io.fp_b(15), Cat(0.U(3.W),io.fp_b(14,10)), Cat(io.fp_b(9,0),0.U(13.W)))
  val fp_a_16to32_is_denormal = !io.fp_a(14,10).orR
  val fp_a_lshift = Wire(UInt(10.W))
  val U_fp_a_is_denormal_to_f32 = Module(new ShiftLeftPriorityWithF32EXPResult(srcW = 10, priorityShiftValueW = 10, expW = 8))
  U_fp_a_is_denormal_to_f32.io.src := io.fp_a(9,0)
  U_fp_a_is_denormal_to_f32.io.priority_shiftValue := io.fp_a(9,0)
  fp_a_lshift := U_fp_a_is_denormal_to_f32.io.lshift_result
  val fp_a_is_denormal_to_f32_exp = U_fp_a_is_denormal_to_f32.io.exp_result
  val fp_a_16to32_mantissa = Cat(Mux(fp_a_16to32_is_denormal,Cat(fp_a_lshift.tail(1),0.U),io.fp_a(9,0)),0.U(13.W))
  val fp_a_16to32_exp = Mux(
    fp_a_16to32_is_denormal,
    fp_a_is_denormal_to_f32_exp,
    Mux(io.fp_a(14), Cat("b1000".U,io.fp_a(13,10)), Cat("b0111".U,io.fp_a(13,10)))
  )
  val fp_a_to32 = Mux(io.res_widening,Cat(io.fp_a(15), fp_a_16to32_exp, fp_a_16to32_mantissa),Mux(res_is_f32,io.fp_a,fp_a_16as32))
  val fp_b_16to32_is_denormal = !io.fp_b(14,10).orR
  val fp_b_lshift = Wire(UInt(10.W))
  val U_fp_b_is_denormal_to_f32 = Module(new ShiftLeftPriorityWithF32EXPResult(srcW = 10, priorityShiftValueW = 10, expW = 8))
  U_fp_b_is_denormal_to_f32.io.src := io.fp_b(9,0)
  U_fp_b_is_denormal_to_f32.io.priority_shiftValue := io.fp_b(9,0)
  fp_b_lshift := U_fp_b_is_denormal_to_f32.io.lshift_result
  val fp_b_is_denormal_to_f32_exp = U_fp_b_is_denormal_to_f32.io.exp_result
  val fp_b_16to32_mantissa = Cat(Mux(fp_b_16to32_is_denormal,Cat(fp_b_lshift.tail(1),0.U),io.fp_b(9,0)),0.U(13.W))
  val fp_b_16to32_exp = Mux(
    fp_b_16to32_is_denormal,
    fp_b_is_denormal_to_f32_exp,
    Mux(io.fp_b(14), Cat("b1000".U,io.fp_b(13,10)), Cat("b0111".U,io.fp_b(13,10)))
  )
  val fp_b_to32 = Mux(io.res_widening & !io.opb_widening,Cat(io.fp_b(15), fp_b_16to32_exp, fp_b_16to32_mantissa),
    Mux(res_is_f32,io.fp_b,fp_b_16as32))

  val EOP = (fp_a_to32.head(1) ^ io.is_sub ^ fp_b_to32.head(1)).asBool
  val U_far_path = Module(new FarPathF32WidenF16MixedPipeline(is_print=is_print, hasMinMaxCompare=hasMinMaxCompare))
  U_far_path.io.fp_a := fp_a_to32
  U_far_path.io.fp_b := fp_b_to32
  U_far_path.io.is_sub := io.is_sub
  U_far_path.io.round_mode := io.round_mode
  U_far_path.io.res_is_f32 := res_is_f32
  val U_close_path = Module(new ClosePathF32WidenF16MixedPipeline(is_print=is_print, hasMinMaxCompare=hasMinMaxCompare))
  U_close_path.io.fp_a := fp_a_to32
  U_close_path.io.fp_b := fp_b_to32
  U_close_path.io.round_mode := io.round_mode
  U_close_path.res_is_f32 := res_is_f32
  val absEaSubEb = U_far_path.io.absEaSubEb
  val is_close_path   =  EOP & (!absEaSubEb(absEaSubEb.getWidth - 1, 1).orR)
  val fp_a_mantissa            = fp_a_to32.tail(1 + exponentWidth)
  val fp_b_mantissa            = fp_b_to32.tail(1 + exponentWidth)
  val fp_a_mantissa_isnot_zero = fp_a_to32.tail(1 + exponentWidth).orR
  val fp_b_mantissa_isnot_zero = fp_b_to32.tail(1 + exponentWidth).orR
  val fp_a_is_f16 = !res_is_f32 | io.res_widening
  val fp_b_is_f16 = !res_is_f32 | (io.res_widening & !io.opb_widening)
  val Efp_a = fp_a_to32(floatWidth-2, floatWidth-1-exponentWidth)
  val Efp_b = fp_b_to32(floatWidth-2, floatWidth-1-exponentWidth)
  val Efp_a_is_zero  = !Efp_a.orR | (fp_a_is_f16 & Efp_a==="b01100110".U)
  val Efp_b_is_zero  = !Efp_b.orR | (fp_b_is_f16 & Efp_b==="b01100110".U)
  val Efp_a_is_all_one   = Mux(fp_a_is_f16,io.fp_a(14,10).andR,io.fp_a(30,23).andR)
  val Efp_b_is_all_one   = Mux(fp_b_is_f16,io.fp_b(14,10).andR,io.fp_b(30,23).andR)
  val fp_a_is_NAN        = Efp_a_is_all_one & fp_a_mantissa_isnot_zero
  val fp_a_is_SNAN       = Efp_a_is_all_one & fp_a_mantissa_isnot_zero & !fp_a_to32(significandWidth-2)
  val fp_b_is_NAN        = Efp_b_is_all_one & fp_b_mantissa_isnot_zero
  val fp_b_is_SNAN       = Efp_b_is_all_one & fp_b_mantissa_isnot_zero & !fp_b_to32(significandWidth-2)
  val fp_a_is_infinite   = Efp_a_is_all_one & (!fp_a_mantissa_isnot_zero)
  val fp_b_is_infinite   = Efp_b_is_all_one & (!fp_b_mantissa_isnot_zero)
  val fp_a_is_zero       = Efp_a_is_zero & !fp_a_mantissa_isnot_zero
  val fp_b_is_zero       = Efp_b_is_zero & !fp_b_mantissa_isnot_zero


  val is_far_path     = !EOP | (EOP & absEaSubEb(absEaSubEb.getWidth - 1, 1).orR) | (absEaSubEb === 1.U & (Efp_a_is_zero ^ Efp_b_is_zero))
  val float_adder_fflags = Wire(UInt(5.W))
  val float_adder_result = Wire(UInt(floatWidth.W))
  when(RegNext((fp_a_is_SNAN | fp_b_is_SNAN) | (EOP & fp_a_is_infinite & fp_b_is_infinite))){
    float_adder_fflags := "b10000".U
  }.elsewhen(RegNext(fp_a_is_NAN | fp_b_is_NAN | fp_a_is_infinite | fp_b_is_infinite | ((fp_b_is_zero | fp_a_is_zero) & io.res_widening)) ){
    float_adder_fflags := "b00000".U
  }.otherwise{
    float_adder_fflags := Mux(RegNext(is_far_path),U_far_path.io.fflags,U_close_path.io.fflags)
  }
  val out_NAN_reg = Mux( RegNext(res_is_f32), Cat(0.U,Fill(8,1.U),1.U,0.U(22.W)), Cat(0.U(17.W),Fill(5,1.U),1.U,0.U(9.W)) )
  val out_infinite_sign = Mux(fp_a_is_infinite,fp_a_to32.head(1),io.is_sub^fp_b_to32.head(1))
  val out_infinite_reg = Mux( RegNext(res_is_f32), Cat(RegNext(out_infinite_sign),Fill(8,1.U),0.U(23.W)), Cat(0.U(16.W),RegNext(out_infinite_sign),Fill(5,1.U),0.U(10.W)) )
  val out_fp32_reg = Mux(RegNext(is_far_path),U_far_path.io.fp_c,U_close_path.io.fp_c)
  val out_fp32_to_fp16_or_fp32_reg = Mux(RegNext(res_is_f32), out_fp32_reg, Cat(0.U(16.W),out_fp32_reg(31),out_fp32_reg(27,23),out_fp32_reg(22,13)) )
  when(RegNext(fp_a_is_NAN | fp_b_is_NAN | (EOP & fp_a_is_infinite & fp_b_is_infinite)) ){
    float_adder_result := out_NAN_reg
  }.elsewhen(RegNext(fp_a_is_infinite | fp_b_is_infinite)) {
    float_adder_result := out_infinite_reg
  }.elsewhen(RegNext(io.res_widening & fp_a_is_zero & fp_b_is_zero)){
    float_adder_result := Cat(RegNext(Mux(io.round_mode==="b010".U & EOP | (fp_a_to32.head(1).asBool & !EOP),1.U,0.U)),0.U(31.W))
  }.elsewhen(RegNext(io.res_widening & fp_a_is_zero)){
    float_adder_result := RegNext(Cat(io.is_sub ^ fp_b_to32.head(1),fp_b_to32(30,0)))
  }.elsewhen(RegNext(io.res_widening & fp_b_is_zero)){
    float_adder_result := RegNext(fp_a_to32)
  }.otherwise{
    float_adder_result := out_fp32_to_fp16_or_fp32_reg
  }
  if (hasMinMaxCompare) {
    val is_add = io.op_code === 0.U
    val is_min = io.op_code === 1.U
    val is_max = io.op_code === 2.U
    val is_feq = io.op_code === 3.U
    val is_fne = io.op_code === 4.U
    val is_flt = io.op_code === 5.U
    val is_fle = io.op_code === 6.U
    val is_fgt = io.op_code === 7.U
    val is_fge = io.op_code === 8.U
    val fp_a_sign = fp_a_to32.head(1)
    val fp_b_sign = fp_b_to32.head(1)
    val fp_b_sign_is_greater = fp_a_sign & !fp_b_sign
    val fp_b_exponent_is_greater = U_far_path.io.isEfp_bGreater
    val fp_b_significand_is_greater = !U_close_path.io.CS1.head(1) & (fp_a_mantissa =/= fp_b_mantissa)
    val fp_b_is_greater = fp_b_sign_is_greater |
      !fp_b_sign_is_greater & fp_b_exponent_is_greater |
      !fp_b_sign_is_greater & !fp_b_exponent_is_greater & fp_b_significand_is_greater
    val fp_b_is_equal = fp_a_to32 === fp_b_to32
    val fp_b_is_less = !fp_b_is_greater & !fp_b_is_equal
    val result_min = Wire(UInt(floatWidth.W))
    val result_max = Wire(UInt(floatWidth.W))
    val result_feq = Wire(UInt(floatWidth.W))
    val result_fne = Wire(UInt(floatWidth.W))
    val result_flt = Wire(UInt(floatWidth.W))
    val result_fle = Wire(UInt(floatWidth.W))
    val result_fgt = Wire(UInt(floatWidth.W))
    val result_fge = Wire(UInt(floatWidth.W))
    val out_NAN = Mux(res_is_f32, Cat(0.U,Fill(8,1.U),1.U,0.U(22.W)), Cat(0.U(17.W),Fill(5,1.U),1.U,0.U(9.W)))
    result_min := Mux1H(
      Seq(
        !fp_a_is_NAN & !fp_b_is_NAN,
        !fp_a_is_NAN &  fp_b_is_NAN,
        fp_a_is_NAN & !fp_b_is_NAN,
        fp_a_is_NAN &  fp_b_is_NAN,
      ),
      Seq(
        Mux(fp_b_is_less,fp_b_to32,fp_a_to32),
        fp_a_to32,
        fp_b_to32,
        out_NAN
      )
    )
    result_max := Mux1H(
      Seq(
        !fp_a_is_NAN & !fp_b_is_NAN,
        !fp_a_is_NAN &  fp_b_is_NAN,
        fp_a_is_NAN & !fp_b_is_NAN,
        fp_a_is_NAN &  fp_b_is_NAN,
      ),
      Seq(
        Mux(fp_b_is_greater.asBool,fp_b_to32,fp_a_to32),
        fp_a_to32,
        fp_b_to32,
        out_NAN
      )
    )
    result_feq := Mux(fp_a_is_NAN | fp_b_is_NAN,0.U,fp_b_is_equal)
    result_fne := !result_feq
    result_flt := Mux(fp_a_is_NAN | fp_b_is_NAN,0.U,fp_b_is_greater)
    result_fle := Mux(fp_a_is_NAN | fp_b_is_NAN,0.U,fp_b_is_greater | fp_b_is_equal)
    result_fgt := Mux(fp_a_is_NAN | fp_b_is_NAN,0.U,fp_b_is_less)
    result_fge := Mux(fp_a_is_NAN | fp_b_is_NAN,0.U,fp_b_is_less | fp_b_is_equal)
    val result_stage0 = Mux1H(
      Seq(
        is_min,
        is_max,
        is_feq,
        is_fne,
        is_flt,
        is_fle,
        is_fgt,
        is_fge
      ),
      Seq(
        result_min,
        result_max,
        result_feq,
        result_fne,
        result_flt,
        result_fle,
        result_fgt,
        result_fge
      )
    )
    val fflags_NV_stage0 = ((is_min | is_max) & (fp_a_is_SNAN | fp_b_is_SNAN)) |
      ((is_feq | is_fne) & (fp_a_is_SNAN | fp_b_is_SNAN)) |
      ((is_flt | is_fle | is_fgt | is_fge) & (fp_a_is_NAN | fp_b_is_NAN))
    val fflags_stage0 = Cat(fflags_NV_stage0,0.U(4.W))
    io.fp_c := Mux(RegNext(is_add),float_adder_result,RegNext(result_stage0))
    io.fflags := Mux(RegNext(is_add),float_adder_result,RegNext(fflags_stage0))
  }
  else {
    io.fp_c := float_adder_result
    io.fflags := float_adder_fflags
  }
}

private[this] class ShiftLeftPriorityWithF32EXPResult(val srcW:Int, priorityShiftValueW:Int, expW:Int) extends Module {
  val io = IO(new Bundle() {
    val src        = Input (UInt(srcW.W))
    val priority_shiftValue = Input (UInt(priorityShiftValueW.W))
    val lshift_result  = Output(UInt(srcW.W))
    val exp_result     = Output(UInt(expW.W))
  })
  def shiftLeftPriorityWithF32EXPResult(srcValue: UInt, priorityShiftValue: UInt): UInt = {
    val width = srcValue.getWidth
    val lzdWidth = srcValue.getWidth.U.getWidth
    def do_shiftLeftPriority(srcValue: UInt, priorityShiftValue: UInt, i:Int): UInt = {
      if (i==0) Cat(
        Mux(
          priorityShiftValue(i),
          Cat(srcValue(0),0.U((width-1).W)),
          0.U(width.W)
        ),
        Mux(
          priorityShiftValue(i),
          "b01110000".U-(width-i-1).U(8.W),
          "b01110000".U-(width-i).U(8.W)
        )
      )
      else Mux(
        priorityShiftValue(i),
        if (i==width-1) Cat(srcValue(i,0),"b01110000".U-(width-i-1).U(8.W)) else Cat(Cat(srcValue(i,0),0.U((width-1-i).W)),"b01110000".U-(width-i-1).U(8.W)),
        do_shiftLeftPriority(srcValue = srcValue, priorityShiftValue = priorityShiftValue, i = i - 1)
      )
    }
    do_shiftLeftPriority(srcValue = srcValue, priorityShiftValue = priorityShiftValue, i = width-1)
  }
  val lshift_result_lzd_exp = shiftLeftPriorityWithF32EXPResult(io.src,io.priority_shiftValue)
  io.lshift_result := lshift_result_lzd_exp.head(srcW)
  io.exp_result := lshift_result_lzd_exp.tail(srcW)
}

private[this] class FarPathF32WidenF16MixedAdderPipeline(val AW:Int, val AdderType:String, val stage0AdderWidth: Int = 0) extends Module {
  val io = IO(new Bundle() {
    val A   = Input (UInt(AW.W))
    val B   = Input (UInt(AW.W))
    val result = Output(UInt(AW.W))
  })
  if (stage0AdderWidth == 0) io.result  := RegNext(io.A) + RegNext(io.B)
  else if (stage0AdderWidth > 0) {
    val short_adder_num = AW/stage0AdderWidth + 1
    val seq_short_full_adder = (for (i <- 0 until short_adder_num) yield {
      if (i == (short_adder_num - 1)) io.A(AW-1,stage0AdderWidth*i) + io.B(AW-1,stage0AdderWidth*i)
      else io.A(stage0AdderWidth*(i+1)-1,stage0AdderWidth*i) +& io.B(stage0AdderWidth*(i+1)-1,stage0AdderWidth*i)
    }).reverse.reduce(Cat(_,_))
    val reg_short_adder_sum = (for (i <- 0 until short_adder_num) yield {
      if (i == (short_adder_num - 1)) seq_short_full_adder(seq_short_full_adder.getWidth-1,(stage0AdderWidth+1)*i)
      else seq_short_full_adder((stage0AdderWidth+1)*i+stage0AdderWidth-1,(stage0AdderWidth+1)*i)
    }).reverse.reduce(Cat(_,_))
    val reg_short_adder_car = (for (i <- 0 until short_adder_num) yield {
      if (i == (short_adder_num - 1)) 0.U((AW-stage0AdderWidth*i-1).W)
      else Cat(seq_short_full_adder((stage0AdderWidth+1)*(i+1)-1),0.U((stage0AdderWidth-1).W))
    }).reverse.reduce(Cat(_,_))
    io.result := Cat(
      RegNext(reg_short_adder_sum(AW-1,stage0AdderWidth)) + RegNext(Cat(reg_short_adder_car,0.U)(AW-1,stage0AdderWidth)),
      RegNext(reg_short_adder_sum(stage0AdderWidth-1,0))
    )
  }
}

private[this] class Adder(val aw:Int,val bw:Int,val cw:Int, val is_sub:Boolean = false) extends Module {
  val io = IO(new Bundle() {
    val a = Input (UInt(aw.W))
    val b = Input (UInt(bw.W))
    val c = Output(UInt(cw.W))
  })
  if (cw > aw) {
    io.c := (if (is_sub) io.a -& io.b else io.a +& io.b)
  }
  else {
    io.c := (if (is_sub) io.a - io.b else io.a + io.b)
  }
}

private[this] class FarShiftRightWithMuxInvFirst(val srcW:Int,shiftValueW:Int) extends Module {
  val io = IO(new Bundle() {
    val src        = Input (UInt(srcW.W))
    val shiftValue = Input (UInt(shiftValueW.W))
    val result     = Output(UInt())
    val EOP        = Input(Bool())
  })
  def shiftRightWithMuxInvFirst(srcValue: UInt, shiftValue: UInt, EOP:Bool): UInt = {
    val vecLength  = shiftValue.getWidth + 1
    val res_vec    = Wire(Vec(vecLength,UInt(srcValue.getWidth.W)))
    res_vec(0)    := srcValue
    for (i <- 0 until shiftValue.getWidth) {
      res_vec(i+1) := Mux(shiftValue(i), Cat(Fill(1<<i,EOP),res_vec(i) >> (1<<i)), res_vec(i))
    }
    res_vec(vecLength-1)
  }
  io.result := shiftRightWithMuxInvFirst(io.src,io.shiftValue,io.EOP)
}

private[this] class FarPathF32WidenF16MixedPipeline(
                                       exponentWidth : Int = 8,
                                       significandWidth : Int = 24,
                                       val is_print:Boolean = false,
                                       val hasMinMaxCompare:Boolean = false
                                     ) extends Module {
  val floatWidth = exponentWidth + significandWidth
  val io = IO(new Bundle() {
    val fp_a, fp_b  = Input (UInt(floatWidth.W))
    val fp_c        = Output(UInt(floatWidth.W))
    val is_sub      = Input (Bool())
    val round_mode  = Input (UInt(3.W))
    val fflags      = Output(UInt(5.W))
    val absEaSubEb  = Output(UInt(exponentWidth.W))
    val res_is_f32  = Input (Bool())
    val isEfp_bGreater = if (hasMinMaxCompare) Output(UInt(1.W)) else Output(UInt(0.W))
  })
  val res_is_f32 = io.res_is_f32
  val res_is_f32_reg = RegNext(res_is_f32)
  val fp_a_sign = io.fp_a.head(1).asBool
  val fp_b_sign = io.fp_b.head(1).asBool
  val efficient_fp_b_sign = (fp_b_sign ^ io.is_sub).asBool
  val EOP = ( fp_a_sign ^ efficient_fp_b_sign ).asBool
  val RNE_reg = RegNext(io.round_mode === "b000".U)  //Round to Nearest, ties to Even
  val RTZ_reg = RegNext(io.round_mode === "b001".U)  //Round towards Zero
  val RDN_reg = RegNext(io.round_mode === "b010".U)  //Round Down (towards −∞)
  val RUP_reg = RegNext(io.round_mode === "b011".U)  //Round Up (towards +∞)
  val RMM_reg = RegNext(io.round_mode === "b100".U)  //Round to Nearest, ties to Max Magnitude
  val NV  = WireInit(false.B)  //Invalid Operation
  val DZ  = WireInit(false.B)  //Divide by Zero
  val OF_reg  = WireInit(false.B)  //Overflow
  val UF  = WireInit(false.B)  //Underflow
  val NX  = WireInit(false.B)  //Inexact
  io.fflags := Cat(NV,DZ,OF_reg,UF,NX)
  val fp_a_mantissa            = io.fp_a.tail(1 + exponentWidth)
  val fp_b_mantissa            = io.fp_b.tail(1 + exponentWidth)
  val fp_a_mantissa_isnot_zero = io.fp_a.tail(1 + exponentWidth).orR
  val fp_b_mantissa_isnot_zero = io.fp_b.tail(1 + exponentWidth).orR
  val Efp_a = io.fp_a(floatWidth-2, floatWidth-1-exponentWidth)
  val Efp_b = io.fp_b(floatWidth-2, floatWidth-1-exponentWidth)
  val Efp_a_is_not_zero  = Efp_a.orR
  val Efp_b_is_not_zero  = Efp_b.orR
  val Efp_a_is_greater_than_1= Efp_a.head(exponentWidth-1).orR
  val Efp_b_is_greater_than_1= Efp_b.head(exponentWidth-1).orR
  val Efp_a_is_all_one   = Efp_a.andR
  val Efp_b_is_all_one   = Efp_b.andR

  val U_Efp_aSubEfp_b = Module(new Adder(Efp_a.getWidth,Efp_a.getWidth,Efp_a.getWidth,is_sub = true))
  U_Efp_aSubEfp_b.io.a := Efp_a | !Efp_a_is_not_zero
  U_Efp_aSubEfp_b.io.b := Efp_b | !Efp_b_is_not_zero
  val Efp_aSubEfp_b     = U_Efp_aSubEfp_b.io.c
  val U_Efp_bSubEfp_a = Module(new Adder(Efp_a.getWidth,Efp_a.getWidth,Efp_a.getWidth,is_sub = true))
  U_Efp_bSubEfp_a.io.a := Efp_b | !Efp_b_is_not_zero
  U_Efp_bSubEfp_a.io.b := Efp_a | !Efp_a_is_not_zero
  val Efp_bSubEfp_a     = U_Efp_bSubEfp_a.io.c
  val isEfp_bGreater    = Efp_b > Efp_a
  val Efp_a_sub_1       = Mux(Efp_a_is_not_zero,Efp_a - 1.U,0.U)
  val Efp_b_sub_1       = Mux(Efp_b_is_not_zero,Efp_b - 1.U,0.U)
  val absEaSubEb        = Wire(UInt(Efp_a.getWidth.W))

  io.absEaSubEb := absEaSubEb
  absEaSubEb := Mux(isEfp_bGreater, Efp_bSubEfp_a, Efp_aSubEfp_b)
  val significand_fp_a   = Cat(Efp_a_is_not_zero,fp_a_mantissa)
  val significand_fp_b   = Cat(Efp_b_is_not_zero,fp_b_mantissa)
  val E_greater          = Mux(isEfp_bGreater, Efp_b, Efp_a)
  val EA                 = Mux(EOP, E_greater-1.U, E_greater)
  val EA_add1            = EA + 1.U
  val greaterSignificand = Mux(isEfp_bGreater, significand_fp_b, significand_fp_a)
  val smallerSignificand = Mux(isEfp_bGreater, significand_fp_a, significand_fp_b)
  val farmaxShiftValue   = (significandWidth+2).U
  val A                  = Mux(EOP,Cat(greaterSignificand,0.U),Cat(0.U,greaterSignificand))

  val widenWidth = significandWidth + 3
  val fp_b_mantissa_widen = Mux(EOP,Cat(~significand_fp_b,1.U,Fill(widenWidth,1.U)),Cat(0.U,significand_fp_b,0.U(widenWidth.W)))
  val U_far_rshift_1 = Module(new FarShiftRightWithMuxInvFirst(fp_b_mantissa_widen.getWidth,farmaxShiftValue.getWidth))
  U_far_rshift_1.io.src := fp_b_mantissa_widen
  U_far_rshift_1.io.shiftValue := Efp_aSubEfp_b.asTypeOf(farmaxShiftValue)
  U_far_rshift_1.io.EOP := EOP
  val U_far_rshift_fp_b_result = U_far_rshift_1.io.result

  val fp_a_mantissa_widen = Mux(EOP,Cat(~significand_fp_a,1.U,Fill(widenWidth,1.U)),Cat(0.U,significand_fp_a,0.U(widenWidth.W)))
  val U_far_rshift_2 = Module(new FarShiftRightWithMuxInvFirst(fp_a_mantissa_widen.getWidth,farmaxShiftValue.getWidth))
  U_far_rshift_2.io.src := fp_a_mantissa_widen
  U_far_rshift_2.io.shiftValue := Efp_bSubEfp_a.asTypeOf(farmaxShiftValue)
  U_far_rshift_2.io.EOP := EOP
  val U_far_rshift_fp_a_result = U_far_rshift_2.io.result

  val far_rshift_widen_result = Mux(isEfp_bGreater,U_far_rshift_fp_a_result,U_far_rshift_fp_b_result)
  val absEaSubEb_is_greater = Mux(res_is_f32,absEaSubEb > (significandWidth + 3).U,absEaSubEb > (11 + 3).U)
  val B = Mux(absEaSubEb_is_greater,Fill(significandWidth+1,EOP),far_rshift_widen_result.head(significandWidth+1))
  val B_guard_normal     = Mux(
    absEaSubEb_is_greater,
    false.B,
    Mux(
      EOP,
      Mux(res_is_f32,!far_rshift_widen_result.head(significandWidth+2)(0).asBool,!far_rshift_widen_result.head(11+2)(0).asBool),
      Mux(res_is_f32,far_rshift_widen_result.head(significandWidth+2)(0).asBool,far_rshift_widen_result.head(11+2)(0).asBool)
    )
  )
  val B_round_normal     = Mux(
    absEaSubEb_is_greater,
    false.B,
    Mux(
      EOP,
      Mux(res_is_f32,!far_rshift_widen_result.head(significandWidth+3)(0).asBool,!far_rshift_widen_result.head(11+3)(0).asBool),
      Mux(res_is_f32,far_rshift_widen_result.head(significandWidth+3)(0).asBool,far_rshift_widen_result.head(11+3)(0).asBool)
    )
  )
  val B_sticky_normal    = Mux(
    absEaSubEb_is_greater,
    smallerSignificand.orR,
    Mux(
      EOP,
      Mux(res_is_f32,(~far_rshift_widen_result.tail(significandWidth+3)).asUInt.orR,(~far_rshift_widen_result.tail(11+3)).asUInt.orR),
      Mux(res_is_f32,far_rshift_widen_result.tail(significandWidth+3).orR,far_rshift_widen_result.tail(11+3).orR)
    )
  )
  val B_rsticky_normal   = B_round_normal | B_sticky_normal
  val B_guard_overflow   = Mux(
    absEaSubEb_is_greater,
    false.B,
    Mux(
      EOP,
      Mux(res_is_f32,!far_rshift_widen_result.head(significandWidth+1)(0).asBool,!far_rshift_widen_result.head(11+1)(0).asBool),
      Mux(res_is_f32,far_rshift_widen_result.head(significandWidth+1)(0).asBool,far_rshift_widen_result.head(11+1)(0).asBool)
    )
  )
  val B_round_overflow   = B_guard_normal
  val B_sticky_overflow  = B_round_normal | B_sticky_normal
  val B_rsticky_overflow = B_round_overflow | B_sticky_overflow
  val U_FS0 = Module(new FarPathF32WidenF16MixedAdderPipeline(AW = A.getWidth, AdderType = "FS0", stage0AdderWidth = 18))
  U_FS0.io.A := A
  U_FS0.io.B := B
  val FS0_reg = U_FS0.io.result
  val U_FS1 = Module(new FarPathF32WidenF16MixedAdderPipeline(AW = A.getWidth, AdderType = "FS1", stage0AdderWidth = 18))
  U_FS1.io.A := A + Mux(res_is_f32,2.U,2.U<<13)
  U_FS1.io.B := B
  val FS1_reg = U_FS1.io.result
  val FS0_0_reg = Mux(res_is_f32_reg,FS0_reg(0),FS0_reg(13))
  val FS0_1_reg = Mux(res_is_f32_reg,FS0_reg(1),FS0_reg(14))
  val far_case_normal_reg   = !FS0_reg.head(1).asBool
  val far_case_overflow_reg = FS0_reg.head(1).asBool
  val lgs_normal_reg = Cat(FS0_0_reg,
    RegNext(
      Mux(
        EOP,
        (~Cat(B_guard_normal,B_rsticky_normal)).asUInt+1.U,
        Cat(B_guard_normal,B_rsticky_normal)
      )
    )
  )

  val far_sign_result_reg = RegNext(Mux(isEfp_bGreater, efficient_fp_b_sign, fp_a_sign))
  val far_case_normal_round_up_reg = (RegNext(EOP) & !lgs_normal_reg(1) & !lgs_normal_reg(0)) |
    (RNE_reg & lgs_normal_reg(1) & (lgs_normal_reg(2) | lgs_normal_reg(0))) |
    (RDN_reg & far_sign_result_reg & (lgs_normal_reg(1) | lgs_normal_reg(0))) |
    (RUP_reg & !far_sign_result_reg & (lgs_normal_reg(1) | lgs_normal_reg(0))) |
    (RMM_reg & lgs_normal_reg(1))
  val normal_fsel0_reg = (!FS0_0_reg & far_case_normal_round_up_reg) | !far_case_normal_round_up_reg
  val normal_fsel1_reg = FS0_0_reg & far_case_normal_round_up_reg
  val A_0 = Mux(res_is_f32,A(0),A(13))
  val B_0 = Mux(res_is_f32,B(0),B(13))
  val grs_overflow_reg = RegNext(Mux(EOP,Cat(A_0,0.U,0.U) - Cat(~B_0,B_guard_normal,B_rsticky_normal),Cat(A_0 ^ B_0,B_guard_normal,B_rsticky_normal)))
  val lgs_overflow_reg = Cat(FS0_1_reg,grs_overflow_reg(2),grs_overflow_reg(1) | grs_overflow_reg(0))
  val far_case_overflow_round_up_reg = (RegNext(EOP) & !lgs_overflow_reg(1) & !lgs_overflow_reg(0)) |
    (RNE_reg & lgs_overflow_reg(1) & (lgs_overflow_reg(2) | lgs_overflow_reg(0))) |
    (RDN_reg & far_sign_result_reg & (lgs_overflow_reg(1) | lgs_overflow_reg(0))) |
    (RUP_reg & !far_sign_result_reg & (lgs_overflow_reg(1) | lgs_overflow_reg(0))) |
    (RMM_reg & lgs_overflow_reg(1))
  val overflow_fsel0_reg = (!FS0_1_reg & far_case_overflow_round_up_reg) | !far_case_overflow_round_up_reg
  val overflow_fsel1_reg =  FS0_1_reg & far_case_overflow_round_up_reg
  val far_exponent_result_reg = Wire(UInt(exponentWidth.W))
  val far_fraction_result_reg = Wire(UInt((significandWidth-1).W))
  far_exponent_result_reg := Mux(
    far_case_overflow_reg | (FS1_reg.head(1).asBool & FS0_0_reg & far_case_normal_round_up_reg) | (RegNext(!EA.orR) & FS0_reg.tail(1).head(1).asBool),
    RegNext(EA_add1),
    RegNext(EA)
  )
  OF_reg := RegNext(Mux(res_is_f32,EA_add1.andR,EA_add1.tail(3).andR)) & (far_case_overflow_reg | (FS1_reg.head(1).asBool & FS0_0_reg & far_case_normal_round_up_reg))
  NX := Mux(far_case_normal_reg,lgs_normal_reg(1,0).orR,lgs_overflow_reg(1,0).orR) | OF_reg
  far_fraction_result_reg := Mux1H(
    Seq(
      far_case_normal_reg & normal_fsel0_reg,
      far_case_normal_reg & normal_fsel1_reg,
      far_case_overflow_reg & overflow_fsel0_reg,
      far_case_overflow_reg & overflow_fsel1_reg
    ),
    Seq(
      Cat(FS0_reg(significandWidth-2,1+13), Mux(res_is_f32_reg,FS0_reg(13),FS0_reg(13) ^ far_case_normal_round_up_reg), FS0_reg(12,1),FS0_0_reg ^ far_case_normal_round_up_reg),
      Cat(FS1_reg(significandWidth-2,1+13), Mux(res_is_f32_reg,FS1_reg(13),0.U), FS1_reg(12,1),0.U),
      Cat(FS0_reg(significandWidth-1,2+13), Mux(res_is_f32_reg,FS0_reg(14),FS0_reg(14) ^ far_case_overflow_round_up_reg), FS0_reg(13,2),FS0_1_reg ^ far_case_overflow_round_up_reg),
      FS1_reg(significandWidth-1,1)
    )
  )
  val result_overflow_reg = Mux(
    RTZ_reg | (RDN_reg & !far_sign_result_reg) | (RUP_reg & far_sign_result_reg),
    Cat(far_sign_result_reg,Fill(exponentWidth-1,1.U),0.U,Fill(significandWidth-1,1.U)),
    Cat(far_sign_result_reg,Fill(exponentWidth,1.U),Fill(significandWidth-1,0.U))
  )
  io.fp_c := Mux(OF_reg,
    result_overflow_reg,
    Cat(far_sign_result_reg,far_exponent_result_reg,far_fraction_result_reg)
  )
  if (hasMinMaxCompare) io.isEfp_bGreater := isEfp_bGreater
}

private[this] class ClosePathF32WidenF16MixedPipelineAdder(val adderWidth:Int, val adderType:String) extends Module {
  val io = IO(new Bundle() {
    val adder_op0    = Input(UInt(adderWidth.W))
    val adder_op1    = Input(UInt(adderWidth.W))
    val result       = Output(UInt((adderWidth+1).W))
  })
  if (adderType == "CS0" | adderType == "CS1") {
    io.result  :=  Cat(0.U,io.adder_op0) - Cat(0.U,io.adder_op1)
  }
  if (adderType == "CS2" | adderType == "CS3") {
    io.result  :=  Cat(io.adder_op0,0.U) - Cat(0.U,io.adder_op1)
  }
}

private[this] object LZD {
  def apply(in: UInt): UInt = PriorityEncoder(Reverse(Cat(in, 1.U)))
}

private[this] class CloseShiftLeftWithMux(val srcW:Int,shiftValueW:Int) extends Module {
  val io = IO(new Bundle() {
    val src        = Input (UInt(srcW.W))
    val shiftValue = Input (UInt(shiftValueW.W))
    val result     = Output(UInt())
  })
  def shiftLeftWithMux(srcValue: UInt, shiftValue: UInt): UInt = {
    val vecLength  = shiftValue.getWidth + 1
    val res_vec    = Wire(Vec(vecLength,UInt(srcValue.getWidth.W)))
    res_vec(0)    := srcValue
    for (i <- 0 until shiftValue.getWidth) {
      res_vec(i+1) := Mux(shiftValue(shiftValue.getWidth-1-i), res_vec(i) << (1<<(shiftValue.getWidth-1-i)), res_vec(i))
    }
    res_vec(vecLength-1)
  }
  io.result := shiftLeftWithMux(io.src,io.shiftValue)
}

private[this] class ClosePathF32WidenF16MixedPipeline(
                                         exponentWidth : Int = 8,
                                         var significandWidth : Int = 24,
                                         val is_print:Boolean = false,
                                         val hasMinMaxCompare:Boolean = false
                                       ) extends Module {
  val floatWidth = exponentWidth + significandWidth
  val io = IO(new Bundle() {
    val fp_a, fp_b  = Input (UInt(floatWidth.W))
    val fp_c        = Output(UInt(floatWidth.W))
    val round_mode  = Input (UInt(3.W))
    val fflags      = Output(UInt(5.W))
    val res_is_f32  = Input (Bool())
    val CS1         = if (hasMinMaxCompare) Output(UInt((significandWidth+1).W)) else Output(UInt(0.W))
  })
  val res_is_f32 = io.res_is_f32
  val fp_a_sign = io.fp_a.head(1).asBool
  val fp_b_sign = io.fp_b.head(1).asBool
  val RNE = io.round_mode === "b000".U
  val RTZ = io.round_mode === "b001".U
  val RDN = io.round_mode === "b010".U
  val RUP = io.round_mode === "b011".U
  val RMM = io.round_mode === "b100".U
  val NV = WireInit(false.B)
  val DZ = WireInit(false.B)
  val OF = WireInit(false.B)
  val UF = WireInit(false.B)
  val NX_reg = WireInit(false.B)
  io.fflags := Cat(NV,DZ,OF,UF,NX_reg)
  val fp_a_mantissa = io.fp_a.tail(1 + exponentWidth)
  val fp_b_mantissa = io.fp_b.tail(1 + exponentWidth)
  val Efp_a = io.fp_a(floatWidth - 2, floatWidth - 1 - exponentWidth)
  val Efp_b = io.fp_b(floatWidth - 2, floatWidth - 1 - exponentWidth)
  val Efp_a_is_not_zero = Efp_a.orR
  val Efp_b_is_not_zero = Efp_b.orR
  val Efp_b_is_greater = (Efp_b(0) ^ Efp_a(0)) & !(Efp_a(1) ^ Efp_b(1) ^ Efp_a(0))
  val E_greater = Mux(Efp_b_is_greater,Efp_b,Efp_a)
  val absEaSubEb = Efp_a(0) ^ Efp_b(0)
  val exp_is_equal = !absEaSubEb | (!Efp_a_is_not_zero ^ !Efp_b_is_not_zero)
  val significand_fp_a = Cat(Efp_a_is_not_zero, fp_a_mantissa)
  val significand_fp_b = Cat(Efp_b_is_not_zero, fp_b_mantissa)
  val EA = Mux(Efp_b_is_greater, Efp_b, Efp_a)

  val B_guard = Mux(Efp_b_is_greater,
    Mux(Efp_a_is_not_zero, Mux(res_is_f32,fp_a_mantissa(0),fp_a_mantissa(0+13)), false.B),
    Mux(Efp_b_is_not_zero, Mux(res_is_f32,fp_b_mantissa(0),fp_b_mantissa(0+13)), false.B)
  )
  val B_round = false.B
  val B_sticky = false.B

  val mask_Efp_a_onehot = Cat(
    Efp_a === 0.U | Efp_a === 1.U,
    (for (i <- 2 until significandWidth + 1) yield
      (Efp_a === i.U).asUInt
      ).reduce(Cat(_, _))
  )
  val mask_Efp_b_onehot = Cat(
    Efp_b === 0.U | Efp_b === 1.U,
    (for (i <- 2 until significandWidth + 1) yield
      (Efp_b === i.U).asUInt
      ).reduce(Cat(_, _))
  )

  val U_CS0 = Module(new ClosePathF32WidenF16MixedPipelineAdder(adderWidth = significandWidth, adderType = "CS0"))
  U_CS0.io.adder_op0 := significand_fp_a
  U_CS0.io.adder_op1 := significand_fp_b
  val CS0 = U_CS0.io.result(significandWidth-1,0)
  val priority_lshift_0 = CS0 | mask_Efp_a_onehot

  val U_CS1 = Module(new ClosePathF32WidenF16MixedPipelineAdder(adderWidth = significandWidth, adderType = "CS1"))
  U_CS1.io.adder_op0 := significand_fp_b
  U_CS1.io.adder_op1 := significand_fp_a
  val CS1 = U_CS1.io.result(significandWidth-1,0)
  val priority_lshift_1 = CS1 | mask_Efp_b_onehot

  val U_CS2 = Module(new ClosePathF32WidenF16MixedPipelineAdder(adderWidth = significandWidth, adderType = "CS2"))
  U_CS2.io.adder_op0 := Cat(1.U,fp_a_mantissa)
  U_CS2.io.adder_op1 := Cat(1.U,fp_b_mantissa)
  val CS2 = U_CS2.io.result(significandWidth,0)
  val priority_lshift_2 = CS2 | Cat(mask_Efp_a_onehot,Efp_a===(significandWidth+1).U)

  val U_CS3 = Module(new ClosePathF32WidenF16MixedPipelineAdder(adderWidth = significandWidth, adderType = "CS3"))
  U_CS3.io.adder_op0 := Cat(1.U,fp_b_mantissa)
  U_CS3.io.adder_op1 := Cat(1.U,fp_a_mantissa)
  val CS3 = U_CS3.io.result(significandWidth,0)
  val priority_lshift_3 = CS3 | Cat(mask_Efp_b_onehot,Efp_b===(significandWidth+1).U)

  val U_CS4 = Module(new ClosePathF32WidenF16MixedPipelineAdder(adderWidth = significandWidth, adderType = "CS0"))
  U_CS4.io.adder_op0 := RegNext(Mux(Efp_b_is_greater,significand_fp_b,significand_fp_a))
  U_CS4.io.adder_op1 := RegNext(Mux(
    Efp_b_is_greater,
    Cat(0.U,Mux(res_is_f32,significand_fp_a(significandWidth-1,1),Cat(significand_fp_a(significandWidth-1,1+13),0.U(13.W)))),
    Cat(0.U,Mux(res_is_f32,significand_fp_b(significandWidth-1,1),Cat(significand_fp_b(significandWidth-1,1+13),0.U(13.W))))
  ))
  val CS4_reg = U_CS4.io.result

  val close_sign_result_reg = Wire(Bool())
  val close_exponent_result_reg = Wire(UInt(exponentWidth.W))
  val close_fraction_result_reg = Wire(UInt((significandWidth - 1).W))
  val CS2_round_up = Mux(res_is_f32,significand_fp_b(0),significand_fp_b(0+13)) & (
    (RUP & !fp_a_sign) | (RDN & fp_a_sign) | (RNE & Mux(res_is_f32,CS2(1),CS2(1+13)) & Mux(res_is_f32,CS2(0),CS2(0+13))) | RMM
    )
  val CS3_round_up = Mux(res_is_f32,significand_fp_a(0),significand_fp_a(0+13)) & (
    (RUP & fp_a_sign) | (RDN & !fp_a_sign) | (RNE & Mux(res_is_f32,CS3(1),CS3(1+13)) & Mux(res_is_f32,CS3(0),CS3(0+13))) | RMM
    )
  val sel_CS0 = exp_is_equal & !U_CS0.io.result.head(1).asBool
  val sel_CS1 = exp_is_equal & U_CS0.io.result.head(1).asBool
  val sel_CS2 = !exp_is_equal & !Efp_b_is_greater & ((!U_CS2.io.result.head(1).asBool | Mux(res_is_f32,!U_CS2.io.result(0),!U_CS2.io.result(0+13)).asBool) | !CS2_round_up)
  val sel_CS3 = !exp_is_equal &  Efp_b_is_greater & ((!U_CS3.io.result.head(1).asBool | Mux(res_is_f32,!U_CS3.io.result(0),!U_CS3.io.result(0+13)).asBool) | !CS3_round_up)
  val sel_CS4 = !exp_is_equal & (
    (!Efp_b_is_greater & U_CS2.io.result.head(1).asBool & Mux(res_is_f32,U_CS2.io.result(0),U_CS2.io.result(0+13)).asBool & CS2_round_up) |
      (Efp_b_is_greater & U_CS3.io.result.head(1).asBool & Mux(res_is_f32,U_CS3.io.result(0),U_CS3.io.result(0+13)).asBool & CS3_round_up)
    )
  val CS_0123_result = Mux1H(
    Seq(
      sel_CS0,
      sel_CS1,
      sel_CS2,
      sel_CS3
    ),
    Seq(
      Cat(CS0,0.U),
      Cat(CS1,0.U),
      CS2,
      CS3
    )
  )
  val mask_onehot = Mux1H(
    Seq(
      sel_CS0,
      sel_CS1,
      sel_CS2,
      sel_CS3
    ),
    Seq(
      Cat(mask_Efp_a_onehot,1.U),
      Cat(mask_Efp_b_onehot,1.U),
      Cat(mask_Efp_a_onehot,Efp_a===(significandWidth+1).U),
      Cat(mask_Efp_b_onehot,Efp_b===(significandWidth+1).U)
    )
  )
  val priority_mask = CS_0123_result | mask_onehot
  val lzd_0123 = LZD(priority_mask)
  val U_Lshift = Module(new CloseShiftLeftWithMux(CS_0123_result.getWidth,priority_mask.getWidth.U.getWidth))
  U_Lshift.io.src := RegNext(CS_0123_result)
  U_Lshift.io.shiftValue := RegNext(lzd_0123)
  val CS_0123_lshift_result_reg = U_Lshift.io.result(significandWidth,1)
  NX_reg := RegNext(Mux(
    (sel_CS2 & (U_CS2.io.result.head(1).asBool & Mux(res_is_f32,U_CS2.io.result(0),U_CS2.io.result(0+13)).asBool & !CS2_round_up)) |
      (sel_CS3 & (U_CS3.io.result.head(1).asBool & Mux(res_is_f32,U_CS3.io.result(0),U_CS3.io.result(0+13)).asBool & !CS3_round_up)) | sel_CS4,
    B_guard,
    0.U
  ))
  close_fraction_result_reg := Mux(RegNext(sel_CS4),CS4_reg.tail(1),CS_0123_lshift_result_reg.tail(1))
  val lshift_result_head_is_one_reg = ((RegNext(EA)-1.U > RegNext(lzd_0123)) & RegNext(CS_0123_result.orR)) | RegNext((mask_onehot & CS_0123_result).orR)
  val EA_sub_value_reg = Mux(
    RegNext(sel_CS4),
    0.U(exponentWidth.W),
    Mux(
      lshift_result_head_is_one_reg,
      RegNext(lzd_0123.asTypeOf(EA)),
      RegNext(EA)
    )
  )
  close_exponent_result_reg := RegNext(EA) - EA_sub_value_reg
  close_sign_result_reg := RegNext(Mux1H(
    Seq(
      sel_CS0 & (exp_is_equal & (U_CS0.io.result.head(1).asBool | U_CS1.io.result.head(1).asBool)),
      sel_CS0 & exp_is_equal & (!U_CS0.io.result.head(1).asBool & !U_CS1.io.result.head(1).asBool),
      sel_CS1,
      sel_CS2,
      sel_CS3,
      sel_CS4
    ),
    Seq(
      fp_a_sign,
      RDN,
      !fp_a_sign,
      fp_a_sign,
      !fp_a_sign,
      Mux(Efp_b_is_greater,!fp_a_sign,fp_a_sign)
    )
  ))
  io.fp_c := Cat(close_sign_result_reg,close_exponent_result_reg,close_fraction_result_reg)
  if (hasMinMaxCompare) io.CS1 := U_CS1.io.result
}

private[this] class ShiftLeftPriorityWithF64EXPResult(val srcW:Int, priorityShiftValueW:Int, expW:Int) extends Module {
  val io = IO(new Bundle() {
    val src        = Input (UInt(srcW.W))
    val priority_shiftValue = Input (UInt(priorityShiftValueW.W))
    val lshift_result  = Output(UInt(srcW.W))
    val exp_result     = Output(UInt(expW.W))
  })
  def shiftLeftPriorityWithF64EXPResult(srcValue: UInt, priorityShiftValue: UInt): UInt = {
    val width = srcValue.getWidth
    val lzdWidth = srcValue.getWidth.U.getWidth
    def do_shiftLeftPriority(srcValue: UInt, priorityShiftValue: UInt, i:Int): UInt = {
      if (i==0) Cat(
        Mux(
          priorityShiftValue(i),
          Cat(srcValue(0),0.U((width-1).W)),
          0.U(width.W)
        ),
        Mux(
          priorityShiftValue(i),
          "b01110000000".U-(width-i-1).U(11.W),
          "b01110000000".U-(width-i).U(11.W)
        )
      )
      else Mux(
        priorityShiftValue(i),
        if (i==width-1) Cat(srcValue(i,0),"b01110000000".U-(width-i-1).U(11.W)) else Cat(Cat(srcValue(i,0),0.U((width-1-i).W)),"b01110000000".U-(width-i-1).U(11.W)),
        do_shiftLeftPriority(srcValue = srcValue, priorityShiftValue = priorityShiftValue, i = i - 1)
      )
    }
    do_shiftLeftPriority(srcValue = srcValue, priorityShiftValue = priorityShiftValue, i = width-1)
  }
  val lshift_result_lzd_exp = shiftLeftPriorityWithF64EXPResult(io.src,io.priority_shiftValue)
  io.lshift_result := lshift_result_lzd_exp.head(srcW)
  io.exp_result := lshift_result_lzd_exp.tail(srcW)
}

private[this] class FarPathAdderF64WidenPipeline(val AW:Int, val AdderType:String, val stage0AdderWidth: Int = 0) extends Module {
  val io = IO(new Bundle() {
    val A   = Input (UInt(AW.W))
    val B   = Input (UInt(AW.W))
    val result = Output(UInt(AW.W))
  })
  if (stage0AdderWidth == 0) io.result  := RegNext(io.A) + RegNext(io.B)
  else if (stage0AdderWidth > 0) {
    val short_adder_num = AW/stage0AdderWidth + 1
    val seq_short_full_adder = (for (i <- 0 until short_adder_num) yield {
      if (i == (short_adder_num - 1)) io.A(AW-1,stage0AdderWidth*i) + io.B(AW-1,stage0AdderWidth*i)
      else io.A(stage0AdderWidth*(i+1)-1,stage0AdderWidth*i) +& io.B(stage0AdderWidth*(i+1)-1,stage0AdderWidth*i)
    }).reverse.reduce(Cat(_,_))
    val reg_short_adder_sum = (for (i <- 0 until short_adder_num) yield {
      if (i == (short_adder_num - 1)) seq_short_full_adder(seq_short_full_adder.getWidth-1,(stage0AdderWidth+1)*i)
      else seq_short_full_adder((stage0AdderWidth+1)*i+stage0AdderWidth-1,(stage0AdderWidth+1)*i)
    }).reverse.reduce(Cat(_,_))
    val reg_short_adder_car = (for (i <- 0 until short_adder_num) yield {
      if (i == (short_adder_num - 1)) 0.U((AW-stage0AdderWidth*i-1).W)
      else Cat(seq_short_full_adder((stage0AdderWidth+1)*(i+1)-1),0.U((stage0AdderWidth-1).W))
    }).reverse.reduce(Cat(_,_))
    io.result := Cat(
      RegNext(reg_short_adder_sum(AW-1,stage0AdderWidth)) + RegNext(Cat(reg_short_adder_car,0.U)(AW-1,stage0AdderWidth)),
      RegNext(reg_short_adder_sum(stage0AdderWidth-1,0))
    )
  }
}

private[this] class FarPathFloatAdderF64WidenPipeline(
                                         exponentWidth : Int = 11,
                                         significandWidth : Int = 53,
                                         val is_print:Boolean = false,
                                         val hasMinMaxCompare:Boolean = false) extends Module {
  val floatWidth = exponentWidth + significandWidth
  val io = IO(new Bundle() {
    val fp_a, fp_b  = Input (UInt(floatWidth.W))
    val fp_c        = Output(UInt(floatWidth.W))
    val is_sub      = Input (Bool())
    val round_mode  = Input (UInt(3.W))
    val fflags      = Output(UInt(5.W))
    val absEaSubEb  = Output(UInt(exponentWidth.W))
    val isEfp_bGreater = if (hasMinMaxCompare) Output(UInt(1.W)) else Output(UInt(0.W))
  })
  val fp_a_sign = io.fp_a.head(1).asBool
  val fp_b_sign = io.fp_b.head(1).asBool
  val efficient_fp_b_sign = (fp_b_sign ^ io.is_sub).asBool
  val EOP = ( fp_a_sign ^ efficient_fp_b_sign ).asBool
  val RNE_reg = RegNext(io.round_mode === "b000".U)
  val RTZ_reg = RegNext(io.round_mode === "b001".U)
  val RDN_reg = RegNext(io.round_mode === "b010".U)
  val RUP_reg = RegNext(io.round_mode === "b011".U)
  val RMM_reg = RegNext(io.round_mode === "b100".U)
  val NV  = WireInit(false.B)
  val DZ  = WireInit(false.B)
  val OF  = WireInit(false.B)
  val UF  = WireInit(false.B)
  val NX  = WireInit(false.B)
  io.fflags := Cat(NV,DZ,OF,UF,NX)
  val fp_a_mantissa            = io.fp_a.tail(1 + exponentWidth)
  val fp_b_mantissa            = io.fp_b.tail(1 + exponentWidth)
  val fp_a_mantissa_isnot_zero = io.fp_a.tail(1 + exponentWidth).orR
  val fp_b_mantissa_isnot_zero = io.fp_b.tail(1 + exponentWidth).orR
  val Efp_a = io.fp_a(floatWidth-2, floatWidth-1-exponentWidth)
  val Efp_b = io.fp_b(floatWidth-2, floatWidth-1-exponentWidth)
  val Efp_a_is_not_zero  = Efp_a.orR
  val Efp_b_is_not_zero  = Efp_b.orR
  val Efp_a_is_greater_than_1= Efp_a.head(exponentWidth-1).orR
  val Efp_b_is_greater_than_1= Efp_b.head(exponentWidth-1).orR
  val Efp_a_is_all_one   = Efp_a.andR
  val Efp_b_is_all_one   = Efp_b.andR
  val U_Efp_aSubEfp_b = Module(new Adder(Efp_a.getWidth,Efp_a.getWidth,Efp_a.getWidth,is_sub = true))
  U_Efp_aSubEfp_b.io.a := Efp_a | !Efp_a_is_not_zero
  U_Efp_aSubEfp_b.io.b := Efp_b | !Efp_b_is_not_zero
  val Efp_aSubEfp_b     = U_Efp_aSubEfp_b.io.c
  val U_Efp_bSubEfp_a = Module(new Adder(Efp_a.getWidth,Efp_a.getWidth,Efp_a.getWidth,is_sub = true))
  U_Efp_bSubEfp_a.io.a := Efp_b | !Efp_b_is_not_zero
  U_Efp_bSubEfp_a.io.b := Efp_a | !Efp_a_is_not_zero
  val Efp_bSubEfp_a     = U_Efp_bSubEfp_a.io.c
  val isEfp_bGreater    = Efp_b > Efp_a
  val Efp_a_sub_1       = Mux(Efp_a_is_not_zero,Efp_a - 1.U,0.U)
  val Efp_b_sub_1       = Mux(Efp_b_is_not_zero,Efp_b - 1.U,0.U)
  val absEaSubEb        = Wire(UInt(Efp_a.getWidth.W))
  io.absEaSubEb := absEaSubEb
  absEaSubEb := Mux(isEfp_bGreater, Efp_bSubEfp_a, Efp_aSubEfp_b)
  val significand_fp_a   = Cat(Efp_a_is_not_zero,fp_a_mantissa)
  val significand_fp_b   = Cat(Efp_b_is_not_zero,fp_b_mantissa)
  val E_greater          = Mux(isEfp_bGreater, Efp_b, Efp_a)
  val EA                 = Mux(EOP, E_greater-1.U, E_greater)
  val EA_add1            = EA + 1.U
  val greaterSignificand = Mux(isEfp_bGreater, significand_fp_b, significand_fp_a)
  val smallerSignificand = Mux(isEfp_bGreater, significand_fp_a, significand_fp_b)
  val farmaxShiftValue   = (significandWidth+2).U
  val A_wire              = Mux(EOP,Cat(greaterSignificand,0.U),Cat(0.U,greaterSignificand))
  val A_wire_FS1 = Mux(EOP,Cat(greaterSignificand,0.U),Cat(0.U,greaterSignificand)) + 2.U
  val widenWidth = significandWidth + 3
  val fp_b_mantissa_widen = Mux(EOP,Cat(~significand_fp_b,1.U,Fill(widenWidth,1.U)),Cat(0.U,significand_fp_b,0.U(widenWidth.W)))
  val U_far_rshift_1 = Module(new FarShiftRightWithMuxInvFirst(fp_b_mantissa_widen.getWidth,farmaxShiftValue.getWidth))
  U_far_rshift_1.io.src := fp_b_mantissa_widen
  U_far_rshift_1.io.shiftValue := Efp_aSubEfp_b.asTypeOf(farmaxShiftValue)
  U_far_rshift_1.io.EOP := EOP
  val U_far_rshift_fp_b_result = U_far_rshift_1.io.result
  val fp_a_mantissa_widen = Mux(EOP,Cat(~significand_fp_a,1.U,Fill(widenWidth,1.U)),Cat(0.U,significand_fp_a,0.U(widenWidth.W)))
  val U_far_rshift_2 = Module(new FarShiftRightWithMuxInvFirst(fp_a_mantissa_widen.getWidth,farmaxShiftValue.getWidth))
  U_far_rshift_2.io.src := fp_a_mantissa_widen
  U_far_rshift_2.io.shiftValue := Efp_bSubEfp_a.asTypeOf(farmaxShiftValue)
  U_far_rshift_2.io.EOP := EOP
  val U_far_rshift_fp_a_result = U_far_rshift_2.io.result

  val far_rshift_widen_result = Mux(isEfp_bGreater,U_far_rshift_fp_a_result,U_far_rshift_fp_b_result)
  val absEaSubEb_is_greater = absEaSubEb > (significandWidth + 3).U
  val B_wire = Mux(absEaSubEb_is_greater,Fill(significandWidth+1,EOP),far_rshift_widen_result.head(significandWidth+1))
  val B_guard_normal_reg     = RegNext(Mux(
    absEaSubEb_is_greater,
    false.B,
    Mux(EOP,!far_rshift_widen_result.head(significandWidth+2)(0).asBool,far_rshift_widen_result.head(significandWidth+2)(0).asBool)
  ))
  val B_round_normal_reg     = RegNext(Mux(
    absEaSubEb_is_greater,
    false.B,
    Mux(EOP,!far_rshift_widen_result.head(significandWidth+3)(0).asBool,far_rshift_widen_result.head(significandWidth+3)(0).asBool)
  ))
  val B_sticky_normal_reg    = Mux(
    RegNext(absEaSubEb_is_greater),
    RegNext(smallerSignificand.orR),
    Mux(RegNext(EOP),RegNext(~far_rshift_widen_result.tail(significandWidth+3)).asUInt.orR,RegNext(far_rshift_widen_result.tail(significandWidth+3)).orR)
  )
  val B_rsticky_normal_reg   = B_round_normal_reg | B_sticky_normal_reg
  val B_guard_overflow_reg   = RegNext(Mux(
    absEaSubEb_is_greater,
    false.B,
    Mux(EOP,!far_rshift_widen_result.head(significandWidth+1)(0).asBool,far_rshift_widen_result.head(significandWidth+1)(0).asBool)
  ))
  val B_round_overflow_reg   = B_guard_normal_reg
  val B_sticky_overflow_reg  = B_round_normal_reg | B_sticky_normal_reg
  val B_rsticky_overflow_reg = B_round_overflow_reg | B_sticky_overflow_reg
  val U_FS0 = Module(new FarPathAdderF64WidenPipeline(AW = significandWidth+1, AdderType = "FS0", stage0AdderWidth = 0))
  U_FS0.io.A := A_wire
  U_FS0.io.B := B_wire
  val FS0 = U_FS0.io.result
  val U_FS1 = Module(new FarPathAdderF64WidenPipeline(AW = significandWidth+1, AdderType = "FS1", stage0AdderWidth = 0))
  U_FS1.io.A := A_wire_FS1
  U_FS1.io.B := B_wire
  val FS1 = U_FS1.io.result
  val far_case_normal   = !FS0.head(1).asBool
  val far_case_overflow = FS0.head(1).asBool
  val lgs_normal_reg = Cat(FS0(0),Mux(RegNext(EOP),(~Cat(B_guard_normal_reg,B_rsticky_normal_reg)).asUInt+1.U,Cat(B_guard_normal_reg,B_rsticky_normal_reg)))

  val far_sign_result_reg = RegNext(Mux(isEfp_bGreater, efficient_fp_b_sign, fp_a_sign))
  val far_case_normal_round_up = (RegNext(EOP) & !lgs_normal_reg(1) & !lgs_normal_reg(0)) |
    (RNE_reg & lgs_normal_reg(1) & (lgs_normal_reg(2) | lgs_normal_reg(0))) |
    (RDN_reg & far_sign_result_reg & (lgs_normal_reg(1) | lgs_normal_reg(0))) |
    (RUP_reg & !far_sign_result_reg & (lgs_normal_reg(1) | lgs_normal_reg(0))) |
    (RMM_reg & lgs_normal_reg(1))
  val normal_fsel0 = (!FS0(0) & far_case_normal_round_up) | !far_case_normal_round_up
  val normal_fsel1 = FS0(0) & far_case_normal_round_up
  val grs_overflow = Mux(RegNext(EOP),Cat(RegNext(A_wire(0)),0.U,0.U) - Cat(RegNext(~B_wire(0)),B_guard_normal_reg,B_rsticky_normal_reg),Cat(FS0(0),B_guard_normal_reg,B_rsticky_normal_reg))
  val lgs_overflow = Cat(FS0(1),grs_overflow(2),grs_overflow(1) | grs_overflow(0))
  val far_case_overflow_round_up = (RegNext(EOP) & !lgs_overflow(1) & !lgs_overflow(0)) |
    (RNE_reg & lgs_overflow(1) & (lgs_overflow(2) | lgs_overflow(0))) |
    (RDN_reg & far_sign_result_reg & (lgs_overflow(1) | lgs_overflow(0))) |
    (RUP_reg & !far_sign_result_reg & (lgs_overflow(1) | lgs_overflow(0))) |
    (RMM_reg & lgs_overflow(1))
  val overflow_fsel0 = (!FS0(1) & far_case_overflow_round_up) | !far_case_overflow_round_up
  val overflow_fsel1 =  FS0(1) & far_case_overflow_round_up
  val far_exponent_result = Wire(UInt(exponentWidth.W))
  val far_fraction_result = Wire(UInt((significandWidth-1).W))
  far_exponent_result := Mux(
    far_case_overflow | (FS1.head(1).asBool & FS0(0) & far_case_normal_round_up) | (RegNext(!EA.orR) & FS0.tail(1).head(1).asBool),
    RegNext(EA_add1),
    RegNext(EA)
  )
  OF := RegNext(EA_add1.andR) & (far_case_overflow | (FS1.head(1).asBool & FS0(0) & far_case_normal_round_up))
  NX := Mux(far_case_normal,lgs_normal_reg(1,0).orR,lgs_overflow(1,0).orR) | OF
  far_fraction_result := Mux1H(
    Seq(
      far_case_normal & normal_fsel0,
      far_case_normal & normal_fsel1,
      far_case_overflow & overflow_fsel0,
      far_case_overflow & overflow_fsel1
    ),
    Seq(
      Cat(FS0(significandWidth-2,1),FS0(0) ^ far_case_normal_round_up),
      Cat(FS1(significandWidth-2,1),0.U),
      Cat(FS0(significandWidth-1,2),FS0(1) ^ far_case_overflow_round_up),
      FS1(significandWidth-1,1)
    )
  )
  val result_overflow = Mux(
    RTZ_reg | (RDN_reg & !far_sign_result_reg) | (RUP_reg & far_sign_result_reg),
    Cat(far_sign_result_reg,Fill(exponentWidth-1,1.U),0.U,Fill(significandWidth-1,1.U)),
    Cat(far_sign_result_reg,Fill(exponentWidth,1.U),Fill(significandWidth-1,0.U))
  )
  io.fp_c := Mux(OF,
    result_overflow,
    Cat(far_sign_result_reg,far_exponent_result,far_fraction_result)
  )
  if (hasMinMaxCompare) io.isEfp_bGreater := isEfp_bGreater
}


private[this] class ClosePathAdder(val adderWidth:Int, val adderType:String) extends Module {
  val io = IO(new Bundle() {
    val adder_op0    = Input(UInt(adderWidth.W))
    val adder_op1    = Input(UInt(adderWidth.W))
    val result       = Output(UInt((adderWidth+1).W))
  })
  if (adderType == "CS0" | adderType == "CS1") {
    io.result  :=  Cat(0.U,io.adder_op0) - Cat(0.U,io.adder_op1)
  }
  if (adderType == "CS2" | adderType == "CS3") {
    io.result  :=  Cat(io.adder_op0,0.U) - Cat(0.U,io.adder_op1)
  }
}

private[this] class ClosePathFloatAdderF64WidenPipeline(
                                           exponentWidth : Int = 11,
                                           var significandWidth : Int = 53,
                                           val is_print:Boolean = false,
                                           val hasMinMaxCompare:Boolean = false) extends Module {
  val floatWidth = exponentWidth + significandWidth
  val io = IO(new Bundle() {
    val fp_a, fp_b  = Input (UInt(floatWidth.W))
    val fp_c        = Output(UInt(floatWidth.W))
    val round_mode  = Input (UInt(3.W))
    val fflags      = Output(UInt(5.W))
    val CS1         = if (hasMinMaxCompare) Output(UInt((significandWidth+1).W)) else Output(UInt(0.W))
  })
  val fp_a_sign = io.fp_a.head(1).asBool
  val fp_b_sign = io.fp_b.head(1).asBool
  val RNE = io.round_mode === "b000".U
  val RTZ = io.round_mode === "b001".U
  val RDN = io.round_mode === "b010".U
  val RUP = io.round_mode === "b011".U
  val RMM = io.round_mode === "b100".U
  val NV = WireInit(false.B)
  val DZ = WireInit(false.B)
  val OF = WireInit(false.B)
  val UF = WireInit(false.B)
  val NX = WireInit(false.B)
  io.fflags := Cat(NV,DZ,OF,UF,NX)
  val fp_a_mantissa = io.fp_a.tail(1 + exponentWidth)
  val fp_b_mantissa = io.fp_b.tail(1 + exponentWidth)
  val Efp_a = io.fp_a(floatWidth - 2, floatWidth - 1 - exponentWidth)
  val Efp_b = io.fp_b(floatWidth - 2, floatWidth - 1 - exponentWidth)
  val Efp_a_is_not_zero = Efp_a.orR
  val Efp_b_is_not_zero = Efp_b.orR
  val Efp_b_is_greater = (Efp_b(0) ^ Efp_a(0)) & !(Efp_a(1) ^ Efp_b(1) ^ Efp_a(0))
  val absEaSubEb = Efp_a(0) ^ Efp_b(0)
  val exp_is_equal = !absEaSubEb | (!Efp_a_is_not_zero ^ !Efp_b_is_not_zero)
  val significand_fp_a = Cat(Efp_a_is_not_zero, fp_a_mantissa)
  val significand_fp_b = Cat(Efp_b_is_not_zero, fp_b_mantissa)
  val EA = Mux(Efp_b_is_greater, Efp_b, Efp_a)

  val B_guard = Mux(Efp_b_is_greater, Mux(Efp_a_is_not_zero, fp_a_mantissa(0), false.B),
    Mux(Efp_b_is_not_zero, fp_b_mantissa(0), false.B))
  val B_round = false.B
  val B_sticky = false.B

  val mask_Efp_a_onehot = Cat(
    Efp_a === 0.U | Efp_a === 1.U,
    (for (i <- 2 until significandWidth + 1) yield
      (Efp_a === i.U).asUInt
      ).reduce(Cat(_, _))
  )
  val mask_Efp_b_onehot = Cat(
    Efp_b === 0.U | Efp_b === 1.U,
    (for (i <- 2 until significandWidth + 1) yield
      (Efp_b === i.U).asUInt
      ).reduce(Cat(_, _))
  )

  val U_CS0 = Module(new ClosePathAdder(adderWidth = significandWidth, adderType = "CS0"))
  U_CS0.io.adder_op0 := significand_fp_a
  U_CS0.io.adder_op1 := significand_fp_b
  val CS0 = U_CS0.io.result(significandWidth-1,0)
  val priority_lshift_0 = CS0 | mask_Efp_a_onehot

  val U_CS1 = Module(new ClosePathAdder(adderWidth = significandWidth, adderType = "CS1"))
  U_CS1.io.adder_op0 := significand_fp_b
  U_CS1.io.adder_op1 := significand_fp_a
  val CS1 = U_CS1.io.result(significandWidth-1,0)
  val priority_lshift_1 = CS1 | mask_Efp_b_onehot

  val U_CS2 = Module(new ClosePathAdder(adderWidth = significandWidth, adderType = "CS2"))
  U_CS2.io.adder_op0 := Cat(1.U,fp_a_mantissa)
  U_CS2.io.adder_op1 := Cat(1.U,fp_b_mantissa)
  val CS2 = U_CS2.io.result(significandWidth,0)
  val priority_lshift_2 = CS2 | Cat(mask_Efp_a_onehot,Efp_a===(significandWidth+1).U)

  val U_CS3 = Module(new ClosePathAdder(adderWidth = significandWidth, adderType = "CS3"))
  U_CS3.io.adder_op0 := Cat(1.U,fp_b_mantissa)
  U_CS3.io.adder_op1 := Cat(1.U,fp_a_mantissa)
  val CS3 = U_CS3.io.result(significandWidth,0)
  val priority_lshift_3 = CS3 | Cat(mask_Efp_b_onehot,Efp_b===(significandWidth+1).U)

  val U_CS4 = Module(new ClosePathAdder(adderWidth = significandWidth, adderType = "CS0"))
  U_CS4.io.adder_op0 := RegNext(Mux(Efp_b_is_greater,significand_fp_b,significand_fp_a))
  U_CS4.io.adder_op1 := RegNext(Mux(Efp_b_is_greater,Cat(0.U,significand_fp_a(significandWidth-1,1)),Cat(0.U,significand_fp_b(significandWidth-1,1))))
  val CS4_reg = U_CS4.io.result

  val close_sign_result = Wire(Bool())
  val close_exponent_result = Wire(UInt(exponentWidth.W))
  val close_fraction_result = Wire(UInt((significandWidth - 1).W))
  val CS2_round_up = significand_fp_b(0) & (
    (RUP & !fp_a_sign) | (RDN & fp_a_sign) | (RNE & CS2(1) & CS2(0)) | RMM
    )
  val CS3_round_up = significand_fp_a(0) & (
    (RUP & fp_a_sign) | (RDN & !fp_a_sign) | (RNE & CS3(1) & CS3(0)) | RMM
    )
  val sel_CS0 = exp_is_equal & !U_CS0.io.result.head(1).asBool
  val sel_CS1 = exp_is_equal & U_CS0.io.result.head(1).asBool
  val sel_CS2 = !exp_is_equal & !Efp_b_is_greater & ((!U_CS2.io.result.head(1).asBool | !U_CS2.io.result(0).asBool) | !CS2_round_up)
  val sel_CS3 = !exp_is_equal &  Efp_b_is_greater & ((!U_CS3.io.result.head(1).asBool | !U_CS3.io.result(0).asBool) | !CS3_round_up)
  val sel_CS4 = !exp_is_equal & (
    (!Efp_b_is_greater & U_CS2.io.result.head(1).asBool & U_CS2.io.result(0).asBool & CS2_round_up) |
      (Efp_b_is_greater & U_CS3.io.result.head(1).asBool & U_CS3.io.result(0).asBool & CS3_round_up)
    )
  val CS_0123_result = Mux1H(
    Seq(
      sel_CS0,
      sel_CS1,
      sel_CS2,
      sel_CS3
    ),
    Seq(
      Cat(CS0,0.U),
      Cat(CS1,0.U),
      CS2,
      CS3
    )
  )
  val mask_onehot = Mux1H(
    Seq(
      sel_CS0,
      sel_CS1,
      sel_CS2,
      sel_CS3
    ),
    Seq(
      Cat(mask_Efp_a_onehot,1.U),
      Cat(mask_Efp_b_onehot,1.U),
      Cat(mask_Efp_a_onehot,Efp_a===(significandWidth+1).U),
      Cat(mask_Efp_b_onehot,Efp_b===(significandWidth+1).U)
    )
  )
  val priority_mask = CS_0123_result | mask_onehot
  val lzd_0123 = LZD(priority_mask)
  val U_Lshift = Module(new CloseShiftLeftWithMux(CS_0123_result.getWidth,priority_mask.getWidth.U.getWidth))
  U_Lshift.io.src := RegNext(CS_0123_result)
  U_Lshift.io.shiftValue := RegNext(lzd_0123)
  val CS_0123_lshift_result_reg = U_Lshift.io.result(significandWidth,1)
  NX := RegNext(Mux(
    (sel_CS2 & (U_CS2.io.result.head(1).asBool & U_CS2.io.result(0).asBool & !CS2_round_up)) |
      (sel_CS3 & (U_CS3.io.result.head(1).asBool & U_CS3.io.result(0).asBool & !CS3_round_up)) | sel_CS4,
    B_guard,
    0.U
  ))
  close_fraction_result := Mux(RegNext(sel_CS4),CS4_reg.tail(1),CS_0123_lshift_result_reg.tail(1))
  val lshift_result_head_is_one = ((RegNext(EA-1.U) > RegNext(lzd_0123)) & RegNext(CS_0123_result).orR) | RegNext(mask_onehot & CS_0123_result).orR
  val EA_sub_value = Mux(
    RegNext(sel_CS4),
    0.U(exponentWidth.W),
    Mux(
      lshift_result_head_is_one,
      RegNext(lzd_0123).asTypeOf(EA),
      RegNext(EA)
    )
  )
  close_exponent_result := RegNext(EA) - EA_sub_value
  close_sign_result := RegNext(Mux1H(
    Seq(
      sel_CS0 & (exp_is_equal & (U_CS0.io.result.head(1).asBool | U_CS1.io.result.head(1).asBool)),
      sel_CS0 & exp_is_equal & (!U_CS0.io.result.head(1).asBool & !U_CS1.io.result.head(1).asBool),
      sel_CS1,
      sel_CS2,
      sel_CS3,
      sel_CS4
    ),
    Seq(
      fp_a_sign,
      RDN,
      !fp_a_sign,
      fp_a_sign,
      !fp_a_sign,
      Mux(Efp_b_is_greater,!fp_a_sign,fp_a_sign)
    )
  ))
  io.fp_c := Cat(close_sign_result,close_exponent_result,close_fraction_result)
  if (hasMinMaxCompare) io.CS1 := U_CS1.io.result
  if (is_print){
    printf(p"*****close path*****\n")
    printf(p"CS1 = ${Binary(CS1)}\n")
    printf(p"CS0 = ${Binary(CS0)}\n")
  }

}

private[vector] class FloatAdderF64WidenPipeline(val is_print:Boolean = false,val hasMinMaxCompare:Boolean = false) extends Module {
  val exponentWidth = 11
  val significandWidth = 53
  val floatWidth = exponentWidth + significandWidth
  val io = IO(new Bundle() {
    val fp_a, fp_b  = Input (UInt(floatWidth.W))
    val fp_c        = Output(UInt(floatWidth.W))
    val is_sub      = Input (Bool())
    val round_mode  = Input (UInt(3.W))
    val fflags      = Output(UInt(5.W))
    val opb_widening = Input (Bool())
    val res_widening = Input (Bool())
    val op_code = if (hasMinMaxCompare) Input(UInt(4.W)) else Input(UInt(0.W))
  })
  val fp_a_to64_is_denormal = !io.fp_a(30,23).orR
  val fp_a_lshift = Wire(UInt(23.W))
  val U_fp_a_is_denormal_to_f64 = Module(new ShiftLeftPriorityWithF64EXPResult(srcW = 23, priorityShiftValueW = 23, expW = 11))
  U_fp_a_is_denormal_to_f64.io.src := io.fp_a(22,0)
  U_fp_a_is_denormal_to_f64.io.priority_shiftValue := io.fp_a(22,0)
  fp_a_lshift := U_fp_a_is_denormal_to_f64.io.lshift_result
  val fp_a_is_denormal_to_f64_exp = U_fp_a_is_denormal_to_f64.io.exp_result
  val fp_a_to64_mantissa = Mux(fp_a_to64_is_denormal,Cat(fp_a_lshift.tail(1),0.U(30.W)),Cat(io.fp_a(22,0),0.U(29.W)))
  val fp_a_to64_exp = Mux(fp_a_to64_is_denormal,
    fp_a_is_denormal_to_f64_exp,
    Mux(io.fp_a(30), Cat("b1000".U,io.fp_a(29,23)), Cat("b0111".U,io.fp_a(29,23)))
  )
  val fp_a_to64 = Mux(io.res_widening,Cat(io.fp_a(31), fp_a_to64_exp, fp_a_to64_mantissa),io.fp_a)
  val fp_b_to64_is_denormal = !io.fp_b(30,23).orR
  val fp_b_lshift = Wire(UInt(23.W))
  val U_fp_b_is_denormal_to_f64 = Module(new ShiftLeftPriorityWithF64EXPResult(srcW = 23, priorityShiftValueW = 23, expW = 11))
  U_fp_b_is_denormal_to_f64.io.src := io.fp_b(22,0)
  U_fp_b_is_denormal_to_f64.io.priority_shiftValue := io.fp_b(22,0)
  fp_b_lshift := U_fp_b_is_denormal_to_f64.io.lshift_result
  val fp_b_is_denormal_to_f64_exp = U_fp_b_is_denormal_to_f64.io.exp_result
  val fp_b_to64_mantissa = Mux(fp_b_to64_is_denormal,Cat(fp_b_lshift.tail(1),0.U(30.W)),Cat(io.fp_b(22,0),0.U(29.W)))
  val fp_b_to64_exp = Mux(fp_b_to64_is_denormal,
    fp_b_is_denormal_to_f64_exp,
    Mux(io.fp_b(30), Cat("b1000".U,io.fp_b(29,23)), Cat("b0111".U,io.fp_b(29,23)))
  )
  val fp_b_to64 = Mux(io.res_widening & !io.opb_widening,Cat(io.fp_b(31), fp_b_to64_exp, fp_b_to64_mantissa),io.fp_b)

  val EOP = (fp_a_to64.head(1) ^ io.is_sub ^ fp_b_to64.head(1)).asBool
  val U_far_path = Module(new FarPathFloatAdderF64WidenPipeline(exponentWidth = exponentWidth,significandWidth = significandWidth, is_print = is_print, hasMinMaxCompare=hasMinMaxCompare))
  U_far_path.io.fp_a := fp_a_to64
  U_far_path.io.fp_b := fp_b_to64
  U_far_path.io.is_sub := io.is_sub
  U_far_path.io.round_mode := io.round_mode
  val U_close_path = Module(new ClosePathFloatAdderF64WidenPipeline(exponentWidth = exponentWidth,significandWidth = significandWidth, is_print = is_print, hasMinMaxCompare=hasMinMaxCompare))
  U_close_path.io.fp_a := fp_a_to64
  U_close_path.io.fp_b := fp_b_to64
  U_close_path.io.round_mode := io.round_mode
  val absEaSubEb = U_far_path.io.absEaSubEb

  val fp_a_mantissa            = fp_a_to64.tail(1 + exponentWidth)
  val fp_b_mantissa            = fp_b_to64.tail(1 + exponentWidth)
  val fp_a_mantissa_isnot_zero = fp_a_to64.tail(1 + exponentWidth).orR
  val fp_b_mantissa_isnot_zero = fp_b_to64.tail(1 + exponentWidth).orR
  val fp_a_is_f32 = io.res_widening
  val fp_b_is_f32 = io.res_widening & !io.opb_widening
  val Efp_a = fp_a_to64(floatWidth-2, floatWidth-1-exponentWidth)
  val Efp_b = fp_b_to64(floatWidth-2, floatWidth-1-exponentWidth)
  val Efp_a_is_zero  = !Efp_a.orR | (fp_a_is_f32 & Efp_a==="b01101101001".U)
  val Efp_b_is_zero  = !Efp_b.orR | (fp_b_is_f32 & Efp_b==="b01101101001".U)
  val Efp_a_is_all_one   = Efp_a.andR | (fp_a_is_f32 & Efp_a==="b10001111111".U)
  val Efp_b_is_all_one   = Efp_b.andR | (fp_b_is_f32 & Efp_b==="b10001111111".U)
  val fp_a_is_NAN        = Efp_a_is_all_one & fp_a_mantissa_isnot_zero
  val fp_a_is_SNAN       = Efp_a_is_all_one & fp_a_mantissa_isnot_zero & !fp_a_to64(significandWidth-2)
  val fp_b_is_NAN        = Efp_b_is_all_one & fp_b_mantissa_isnot_zero
  val fp_b_is_SNAN       = Efp_b_is_all_one & fp_b_mantissa_isnot_zero & !fp_b_to64(significandWidth-2)
  val fp_a_is_infinite   = Efp_a_is_all_one & (!fp_a_mantissa_isnot_zero)
  val fp_b_is_infinite   = Efp_b_is_all_one & (!fp_b_mantissa_isnot_zero)
  val fp_a_is_zero_reg   = RegNext(Efp_a_is_zero & !fp_a_mantissa_isnot_zero)
  val fp_b_is_zero_reg   = RegNext(Efp_b_is_zero & !fp_b_mantissa_isnot_zero)
  val res_widening_reg   = RegNext(io.res_widening)

  val is_far_path_reg     = RegNext(!EOP | absEaSubEb(absEaSubEb.getWidth - 1, 1).orR | (absEaSubEb === 1.U & (Efp_a_is_zero ^ Efp_b_is_zero)))
  val float_adder_fflags = Wire(UInt(5.W))
  val float_adder_result = Wire(UInt(floatWidth.W))
  when(RegNext((fp_a_is_SNAN | fp_b_is_SNAN) | (EOP & fp_a_is_infinite & fp_b_is_infinite))){
    float_adder_fflags := "b10000".U
  }.elsewhen(RegNext(fp_a_is_NAN | fp_b_is_NAN | fp_a_is_infinite | fp_b_is_infinite) | ((fp_b_is_zero_reg | fp_a_is_zero_reg) & res_widening_reg)){
    float_adder_fflags := "b00000".U
  }.otherwise{
    float_adder_fflags := Mux(is_far_path_reg,U_far_path.io.fflags,U_close_path.io.fflags)
  }

  when(RegNext(fp_a_is_NAN | fp_b_is_NAN | (EOP & fp_a_is_infinite & fp_b_is_infinite)) ){
    float_adder_result := RegNext(Cat(0.U,Fill(exponentWidth,1.U),1.U,Fill(significandWidth-2,0.U)))
  }.elsewhen(RegNext(fp_a_is_infinite | fp_b_is_infinite)) {
    float_adder_result := RegNext(Cat(Mux(fp_a_is_infinite,fp_a_to64.head(1),io.is_sub^fp_b_to64.head(1)),Fill(exponentWidth,1.U),Fill(significandWidth-1,0.U)))
  }.elsewhen(res_widening_reg & fp_a_is_zero_reg & fp_b_is_zero_reg){
    float_adder_result := RegNext(Cat(Mux(io.round_mode==="b010".U & EOP | (fp_a_to64.head(1).asBool & !EOP),1.U,0.U),0.U(63.W)))
  }.elsewhen(res_widening_reg & fp_a_is_zero_reg){
    float_adder_result := RegNext(Cat(io.is_sub ^ fp_b_to64.head(1),fp_b_to64(62,0)))
  }.elsewhen(res_widening_reg & fp_b_is_zero_reg){
    float_adder_result := RegNext(fp_a_to64)
  }.otherwise{
    float_adder_result := Mux(is_far_path_reg,U_far_path.io.fp_c,U_close_path.io.fp_c)
  }
  if (hasMinMaxCompare) {
    val is_add = io.op_code === 0.U
    val is_min = io.op_code === 1.U
    val is_max = io.op_code === 2.U
    val is_feq = io.op_code === 3.U
    val is_fne = io.op_code === 4.U
    val is_flt = io.op_code === 5.U
    val is_fle = io.op_code === 6.U
    val is_fgt = io.op_code === 7.U
    val is_fge = io.op_code === 8.U
    val fp_a_sign = io.fp_a.head(1)
    val fp_b_sign = io.fp_b.head(1)
    val fp_b_sign_is_greater = fp_a_sign & !fp_b_sign
    val fp_b_exponent_is_greater = U_far_path.io.isEfp_bGreater
    val fp_b_significand_is_greater = !U_close_path.io.CS1.head(1) & (fp_a_mantissa =/= fp_b_mantissa)
    val fp_b_is_greater = fp_b_sign_is_greater |
      !fp_b_sign_is_greater & fp_b_exponent_is_greater |
      !fp_b_sign_is_greater & !fp_b_exponent_is_greater & fp_b_significand_is_greater
    val fp_b_is_equal = io.fp_a === io.fp_b
    val fp_b_is_less = !fp_b_is_greater & !fp_b_is_equal
    val result_min = Wire(UInt(floatWidth.W))
    val result_max = Wire(UInt(floatWidth.W))
    val result_feq = Wire(UInt(floatWidth.W))
    val result_fne = Wire(UInt(floatWidth.W))
    val result_flt = Wire(UInt(floatWidth.W))
    val result_fle = Wire(UInt(floatWidth.W))
    val result_fgt = Wire(UInt(floatWidth.W))
    val result_fge = Wire(UInt(floatWidth.W))
    val out_NAN = Cat(0.U,Fill(exponentWidth,1.U),1.U,Fill(significandWidth-2,0.U))
    result_min := Mux1H(
      Seq(
        !fp_a_is_NAN & !fp_b_is_NAN,
        !fp_a_is_NAN &  fp_b_is_NAN,
        fp_a_is_NAN & !fp_b_is_NAN,
        fp_a_is_NAN &  fp_b_is_NAN,
      ),
      Seq(
        Mux(fp_b_is_less,io.fp_b,io.fp_a),
        io.fp_a,
        io.fp_b,
        out_NAN
      )
    )
    result_max := Mux1H(
      Seq(
        !fp_a_is_NAN & !fp_b_is_NAN,
        !fp_a_is_NAN &  fp_b_is_NAN,
        fp_a_is_NAN & !fp_b_is_NAN,
        fp_a_is_NAN &  fp_b_is_NAN,
      ),
      Seq(
        Mux(fp_b_is_greater.asBool,io.fp_b,io.fp_a),
        io.fp_a,
        io.fp_b,
        out_NAN
      )
    )
    result_feq := Mux(fp_a_is_NAN | fp_b_is_NAN,0.U,fp_b_is_equal)
    result_fne := !result_feq
    result_flt := Mux(fp_a_is_NAN | fp_b_is_NAN,0.U,fp_b_is_greater)
    result_fle := Mux(fp_a_is_NAN | fp_b_is_NAN,0.U,fp_b_is_greater | fp_b_is_equal)
    result_fgt := Mux(fp_a_is_NAN | fp_b_is_NAN,0.U,fp_b_is_less)
    result_fge := Mux(fp_a_is_NAN | fp_b_is_NAN,0.U,fp_b_is_less | fp_b_is_equal)
    val result_stage0 = Mux1H(
      Seq(
        is_min,
        is_max,
        is_feq,
        is_fne,
        is_flt,
        is_fle,
        is_fgt,
        is_fge
      ),
      Seq(
        result_min,
        result_max,
        result_feq,
        result_fne,
        result_flt,
        result_fle,
        result_fgt,
        result_fge
      )
    )
    val fflags_NV_stage0 = ((is_min | is_max) & (fp_a_is_SNAN | fp_b_is_SNAN)) |
      ((is_feq | is_fne) & (fp_a_is_SNAN | fp_b_is_SNAN)) |
      ((is_flt | is_fle | is_fgt | is_fge) & (fp_a_is_NAN | fp_b_is_NAN))
    val fflags_stage0 = Cat(fflags_NV_stage0,0.U(4.W))
    io.fp_c := Mux(RegNext(is_add),float_adder_result,RegNext(result_stage0))
    io.fflags := Mux(RegNext(is_add),float_adder_result,RegNext(fflags_stage0))
  }
  else {
    io.fp_c := float_adder_result
    io.fflags := float_adder_fflags
  }
}

private[this] class FarPathAdderF16Pipeline(val AW:Int, val AdderType:String, val stage0AdderWidth: Int = 0) extends Module {
  val io = IO(new Bundle() {
    val A   = Input (UInt(AW.W))
    val B   = Input (UInt(AW.W))
    val result = Output(UInt(AW.W))
  })
  if (stage0AdderWidth == 0) io.result  := RegNext(io.A) + RegNext(io.B)
  else if (stage0AdderWidth > 0) {
    val short_adder_num = AW/stage0AdderWidth + 1
    val seq_short_full_adder = (for (i <- 0 until short_adder_num) yield {
      if (i == (short_adder_num - 1)) io.A(AW-1,stage0AdderWidth*i) + io.B(AW-1,stage0AdderWidth*i)
      else io.A(stage0AdderWidth*(i+1)-1,stage0AdderWidth*i) +& io.B(stage0AdderWidth*(i+1)-1,stage0AdderWidth*i)
    }).reverse.reduce(Cat(_,_))
    val reg_short_adder_sum = (for (i <- 0 until short_adder_num) yield {
      if (i == (short_adder_num - 1)) seq_short_full_adder(seq_short_full_adder.getWidth-1,(stage0AdderWidth+1)*i)
      else seq_short_full_adder((stage0AdderWidth+1)*i+stage0AdderWidth-1,(stage0AdderWidth+1)*i)
    }).reverse.reduce(Cat(_,_))
    val reg_short_adder_car = (for (i <- 0 until short_adder_num) yield {
      if (i == (short_adder_num - 1)) 0.U((AW-stage0AdderWidth*i-1).W)
      else Cat(seq_short_full_adder((stage0AdderWidth+1)*(i+1)-1),0.U((stage0AdderWidth-1).W))
    }).reverse.reduce(Cat(_,_))
    io.result := Cat(
      RegNext(reg_short_adder_sum(AW-1,stage0AdderWidth)) + RegNext(Cat(reg_short_adder_car,0.U)(AW-1,stage0AdderWidth)),
      RegNext(reg_short_adder_sum(stage0AdderWidth-1,0))
    )
  }
}

private[this] class FarPathF16Pipeline(
                          exponentWidth : Int = 5,
                          significandWidth : Int = 11,
                          val is_print:Boolean = false,
                          val hasMinMaxCompare:Boolean = false) extends Module {
  val floatWidth = exponentWidth + significandWidth
  val io = IO(new Bundle() {
    val fp_a, fp_b  = Input (UInt(floatWidth.W))
    val fp_c        = Output(UInt(floatWidth.W))
    val is_sub      = Input (Bool())
    val round_mode  = Input (UInt(3.W))
    val fflags      = Output(UInt(5.W))
    val absEaSubEb  = Output(UInt(exponentWidth.W))
    val isEfp_bGreater = if (hasMinMaxCompare) Output(UInt(1.W)) else Output(UInt(0.W))
  })
  val fp_a_sign = io.fp_a.head(1).asBool
  val fp_b_sign = io.fp_b.head(1).asBool
  val efficient_fp_b_sign = (fp_b_sign ^ io.is_sub).asBool
  val EOP = ( fp_a_sign ^ efficient_fp_b_sign ).asBool
  val RNE_reg = RegNext(io.round_mode === "b000".U)
  val RTZ_reg = RegNext(io.round_mode === "b001".U)
  val RDN_reg = RegNext(io.round_mode === "b010".U)
  val RUP_reg = RegNext(io.round_mode === "b011".U)
  val RMM_reg = RegNext(io.round_mode === "b100".U)
  val NV  = WireInit(false.B)
  val DZ  = WireInit(false.B)
  val OF  = WireInit(false.B)
  val UF  = WireInit(false.B)
  val NX  = WireInit(false.B)
  io.fflags := Cat(NV,DZ,OF,UF,NX)
  val fp_a_mantissa            = io.fp_a.tail(1 + exponentWidth)
  val fp_b_mantissa            = io.fp_b.tail(1 + exponentWidth)
  val fp_a_mantissa_isnot_zero = io.fp_a.tail(1 + exponentWidth).orR
  val fp_b_mantissa_isnot_zero = io.fp_b.tail(1 + exponentWidth).orR
  val Efp_a = io.fp_a(floatWidth-2, floatWidth-1-exponentWidth)
  val Efp_b = io.fp_b(floatWidth-2, floatWidth-1-exponentWidth)
  val Efp_a_is_not_zero  = Efp_a.orR
  val Efp_b_is_not_zero  = Efp_b.orR
  val Efp_a_is_greater_than_1= Efp_a.head(exponentWidth-1).orR
  val Efp_b_is_greater_than_1= Efp_b.head(exponentWidth-1).orR
  val Efp_a_is_all_one   = Efp_a.andR
  val Efp_b_is_all_one   = Efp_b.andR
  val U_Efp_aSubEfp_b = Module(new Adder(Efp_a.getWidth,Efp_a.getWidth,Efp_a.getWidth,is_sub = true))
  U_Efp_aSubEfp_b.io.a := Efp_a | !Efp_a_is_not_zero
  U_Efp_aSubEfp_b.io.b := Efp_b | !Efp_b_is_not_zero
  val Efp_aSubEfp_b     = U_Efp_aSubEfp_b.io.c
  val U_Efp_bSubEfp_a = Module(new Adder(Efp_a.getWidth,Efp_a.getWidth,Efp_a.getWidth,is_sub = true))
  U_Efp_bSubEfp_a.io.a := Efp_b | !Efp_b_is_not_zero
  U_Efp_bSubEfp_a.io.b := Efp_a | !Efp_a_is_not_zero
  val Efp_bSubEfp_a     = U_Efp_bSubEfp_a.io.c
  val isEfp_bGreater    = Efp_b > Efp_a
  val Efp_a_sub_1       = Mux(Efp_a_is_not_zero,Efp_a - 1.U,0.U)
  val Efp_b_sub_1       = Mux(Efp_b_is_not_zero,Efp_b - 1.U,0.U)
  val absEaSubEb        = Wire(UInt(Efp_a.getWidth.W))
  io.absEaSubEb := absEaSubEb
  absEaSubEb := Mux(isEfp_bGreater, Efp_bSubEfp_a, Efp_aSubEfp_b)
  val significand_fp_a   = Cat(Efp_a_is_not_zero,fp_a_mantissa)
  val significand_fp_b   = Cat(Efp_b_is_not_zero,fp_b_mantissa)
  val E_greater          = Mux(isEfp_bGreater, Efp_b, Efp_a)
  val EA                 = Mux(EOP, E_greater-1.U, E_greater)
  val EA_add1            = EA + 1.U
  val greaterSignificand = Mux(isEfp_bGreater, significand_fp_b, significand_fp_a)
  val smallerSignificand = Mux(isEfp_bGreater, significand_fp_a, significand_fp_b)
  val farmaxShiftValue   = (significandWidth+2).U
  val A_wire              = Mux(EOP,Cat(greaterSignificand,0.U),Cat(0.U,greaterSignificand))
  val A_wire_FS1 = Mux(EOP,Cat(greaterSignificand,0.U),Cat(0.U,greaterSignificand)) + 2.U
  val widenWidth = significandWidth + 3
  val fp_b_mantissa_widen = Mux(EOP,Cat(~significand_fp_b,1.U,Fill(widenWidth,1.U)),Cat(0.U,significand_fp_b,0.U(widenWidth.W)))
  val U_far_rshift_1 = Module(new FarShiftRightWithMuxInvFirst(fp_b_mantissa_widen.getWidth,farmaxShiftValue.getWidth))
  U_far_rshift_1.io.src := fp_b_mantissa_widen
  U_far_rshift_1.io.shiftValue := Efp_aSubEfp_b.asTypeOf(farmaxShiftValue)
  U_far_rshift_1.io.EOP := EOP
  val U_far_rshift_fp_b_result = U_far_rshift_1.io.result
  val fp_a_mantissa_widen = Mux(EOP,Cat(~significand_fp_a,1.U,Fill(widenWidth,1.U)),Cat(0.U,significand_fp_a,0.U(widenWidth.W)))
  val U_far_rshift_2 = Module(new FarShiftRightWithMuxInvFirst(fp_a_mantissa_widen.getWidth,farmaxShiftValue.getWidth))
  U_far_rshift_2.io.src := fp_a_mantissa_widen
  U_far_rshift_2.io.shiftValue := Efp_bSubEfp_a.asTypeOf(farmaxShiftValue)
  U_far_rshift_2.io.EOP := EOP
  val U_far_rshift_fp_a_result = U_far_rshift_2.io.result

  val far_rshift_widen_result = Mux(isEfp_bGreater,U_far_rshift_fp_a_result,U_far_rshift_fp_b_result)
  val absEaSubEb_is_greater = absEaSubEb > (significandWidth + 3).U
  val B_wire = Mux(absEaSubEb_is_greater,Fill(significandWidth+1,EOP),far_rshift_widen_result.head(significandWidth+1))
  val B_guard_normal_reg     = RegNext(Mux(
    absEaSubEb_is_greater,
    false.B,
    Mux(EOP,!far_rshift_widen_result.head(significandWidth+2)(0).asBool,far_rshift_widen_result.head(significandWidth+2)(0).asBool)
  ))
  val B_round_normal_reg     = RegNext(Mux(
    absEaSubEb_is_greater,
    false.B,
    Mux(EOP,!far_rshift_widen_result.head(significandWidth+3)(0).asBool,far_rshift_widen_result.head(significandWidth+3)(0).asBool)
  ))
  val B_sticky_normal_reg    = Mux(
    RegNext(absEaSubEb_is_greater),
    RegNext(smallerSignificand.orR),
    Mux(RegNext(EOP),RegNext(~far_rshift_widen_result.tail(significandWidth+3)).asUInt.orR,RegNext(far_rshift_widen_result.tail(significandWidth+3)).orR)
  )
  val B_rsticky_normal_reg   = B_round_normal_reg | B_sticky_normal_reg
  val B_guard_overflow_reg   = RegNext(Mux(
    absEaSubEb_is_greater,
    false.B,
    Mux(EOP,!far_rshift_widen_result.head(significandWidth+1)(0).asBool,far_rshift_widen_result.head(significandWidth+1)(0).asBool)
  ))
  val B_round_overflow_reg   = B_guard_normal_reg
  val B_sticky_overflow_reg  = B_round_normal_reg | B_sticky_normal_reg
  val B_rsticky_overflow_reg = B_round_overflow_reg | B_sticky_overflow_reg
  val U_FS0 = Module(new FarPathAdderF16Pipeline(AW = significandWidth+1, AdderType = "FS0", stage0AdderWidth = 0))
  U_FS0.io.A := A_wire
  U_FS0.io.B := B_wire
  val FS0 = U_FS0.io.result
  val U_FS1 = Module(new FarPathAdderF16Pipeline(AW = significandWidth+1, AdderType = "FS1", stage0AdderWidth = 0))
  U_FS1.io.A := A_wire_FS1
  U_FS1.io.B := B_wire
  val FS1 = U_FS1.io.result
  val far_case_normal   = !FS0.head(1).asBool
  val far_case_overflow = FS0.head(1).asBool
  val lgs_normal_reg = Cat(FS0(0),Mux(RegNext(EOP),(~Cat(B_guard_normal_reg,B_rsticky_normal_reg)).asUInt+1.U,Cat(B_guard_normal_reg,B_rsticky_normal_reg)))

  val far_sign_result_reg = RegNext(Mux(isEfp_bGreater, efficient_fp_b_sign, fp_a_sign))
  val far_case_normal_round_up = (RegNext(EOP) & !lgs_normal_reg(1) & !lgs_normal_reg(0)) |
    (RNE_reg & lgs_normal_reg(1) & (lgs_normal_reg(2) | lgs_normal_reg(0))) |
    (RDN_reg & far_sign_result_reg & (lgs_normal_reg(1) | lgs_normal_reg(0))) |
    (RUP_reg & !far_sign_result_reg & (lgs_normal_reg(1) | lgs_normal_reg(0))) |
    (RMM_reg & lgs_normal_reg(1))
  val normal_fsel0 = (!FS0(0) & far_case_normal_round_up) | !far_case_normal_round_up
  val normal_fsel1 = FS0(0) & far_case_normal_round_up
  val grs_overflow = Mux(RegNext(EOP),Cat(RegNext(A_wire(0)),0.U,0.U) - Cat(RegNext(~B_wire(0)),B_guard_normal_reg,B_rsticky_normal_reg),Cat(FS0(0),B_guard_normal_reg,B_rsticky_normal_reg))
  val lgs_overflow = Cat(FS0(1),grs_overflow(2),grs_overflow(1) | grs_overflow(0))
  val far_case_overflow_round_up = (RegNext(EOP) & !lgs_overflow(1) & !lgs_overflow(0)) |
    (RNE_reg & lgs_overflow(1) & (lgs_overflow(2) | lgs_overflow(0))) |
    (RDN_reg & far_sign_result_reg & (lgs_overflow(1) | lgs_overflow(0))) |
    (RUP_reg & !far_sign_result_reg & (lgs_overflow(1) | lgs_overflow(0))) |
    (RMM_reg & lgs_overflow(1))
  val overflow_fsel0 = (!FS0(1) & far_case_overflow_round_up) | !far_case_overflow_round_up
  val overflow_fsel1 =  FS0(1) & far_case_overflow_round_up
  val far_exponent_result = Wire(UInt(exponentWidth.W))
  val far_fraction_result = Wire(UInt((significandWidth-1).W))
  far_exponent_result := Mux(
    far_case_overflow | (FS1.head(1).asBool & FS0(0) & far_case_normal_round_up) | (RegNext(!EA.orR) & FS0.tail(1).head(1).asBool),
    RegNext(EA_add1),
    RegNext(EA)
  )
  OF := RegNext(EA_add1.andR) & (far_case_overflow | (FS1.head(1).asBool & FS0(0) & far_case_normal_round_up))
  NX := Mux(far_case_normal,lgs_normal_reg(1,0).orR,lgs_overflow(1,0).orR) | OF
  far_fraction_result := Mux1H(
    Seq(
      far_case_normal & normal_fsel0,
      far_case_normal & normal_fsel1,
      far_case_overflow & overflow_fsel0,
      far_case_overflow & overflow_fsel1
    ),
    Seq(
      Cat(FS0(significandWidth-2,1),FS0(0) ^ far_case_normal_round_up),
      Cat(FS1(significandWidth-2,1),0.U),
      Cat(FS0(significandWidth-1,2),FS0(1) ^ far_case_overflow_round_up),
      FS1(significandWidth-1,1)
    )
  )
  val result_overflow = Mux(
    RTZ_reg | (RDN_reg & !far_sign_result_reg) | (RUP_reg & far_sign_result_reg),
    Cat(far_sign_result_reg,Fill(exponentWidth-1,1.U),0.U,Fill(significandWidth-1,1.U)),
    Cat(far_sign_result_reg,Fill(exponentWidth,1.U),Fill(significandWidth-1,0.U))
  )
  io.fp_c := Mux(OF,
    result_overflow,
    Cat(far_sign_result_reg,far_exponent_result,far_fraction_result)
  )
  if (hasMinMaxCompare) io.isEfp_bGreater := isEfp_bGreater
}

private[this] class ClosePathAdderF16Pipeline(val adderWidth:Int, val adderType:String) extends Module {
  val io = IO(new Bundle() {
    val adder_op0    = Input(UInt(adderWidth.W))
    val adder_op1    = Input(UInt(adderWidth.W))
    val result       = Output(UInt((adderWidth+1).W))
  })
  if (adderType == "CS0" | adderType == "CS1") {
    io.result  :=  Cat(0.U,io.adder_op0) - Cat(0.U,io.adder_op1)
  }
  if (adderType == "CS2" | adderType == "CS3") {
    io.result  :=  Cat(io.adder_op0,0.U) - Cat(0.U,io.adder_op1)
  }
}

private[this] class ClosePathF16Pipeline(
                            exponentWidth : Int = 11,
                            var significandWidth : Int = 53,
                            val is_print:Boolean = false,
                            val hasMinMaxCompare:Boolean = false) extends Module {
  val floatWidth = exponentWidth + significandWidth
  val io = IO(new Bundle() {
    val fp_a, fp_b  = Input (UInt(floatWidth.W))
    val fp_c        = Output(UInt(floatWidth.W))
    val round_mode  = Input (UInt(3.W))
    val fflags      = Output(UInt(5.W))
    val CS1         = if (hasMinMaxCompare) Output(UInt((significandWidth+1).W)) else Output(UInt(0.W))
  })
  val fp_a_sign = io.fp_a.head(1).asBool
  val fp_b_sign = io.fp_b.head(1).asBool
  val RNE = io.round_mode === "b000".U
  val RTZ = io.round_mode === "b001".U
  val RDN = io.round_mode === "b010".U
  val RUP = io.round_mode === "b011".U
  val RMM = io.round_mode === "b100".U
  val NV = WireInit(false.B)
  val DZ = WireInit(false.B)
  val OF = WireInit(false.B)
  val UF = WireInit(false.B)
  val NX = WireInit(false.B)
  io.fflags := Cat(NV,DZ,OF,UF,NX)
  val fp_a_mantissa = io.fp_a.tail(1 + exponentWidth)
  val fp_b_mantissa = io.fp_b.tail(1 + exponentWidth)
  val Efp_a = io.fp_a(floatWidth - 2, floatWidth - 1 - exponentWidth)
  val Efp_b = io.fp_b(floatWidth - 2, floatWidth - 1 - exponentWidth)
  val Efp_a_is_not_zero = Efp_a.orR
  val Efp_b_is_not_zero = Efp_b.orR
  val Efp_b_is_greater = (Efp_b(0) ^ Efp_a(0)) & !(Efp_a(1) ^ Efp_b(1) ^ Efp_a(0))
  val absEaSubEb = Efp_a(0) ^ Efp_b(0)
  val exp_is_equal = !absEaSubEb | (!Efp_a_is_not_zero ^ !Efp_b_is_not_zero)
  val significand_fp_a = Cat(Efp_a_is_not_zero, fp_a_mantissa)
  val significand_fp_b = Cat(Efp_b_is_not_zero, fp_b_mantissa)
  val EA = Mux(Efp_b_is_greater, Efp_b, Efp_a)

  val B_guard = Mux(Efp_b_is_greater, Mux(Efp_a_is_not_zero, fp_a_mantissa(0), false.B),
    Mux(Efp_b_is_not_zero, fp_b_mantissa(0), false.B))
  val B_round = false.B
  val B_sticky = false.B

  val mask_Efp_a_onehot = Cat(
    Efp_a === 0.U | Efp_a === 1.U,
    (for (i <- 2 until significandWidth + 1) yield
      (Efp_a === i.U).asUInt
      ).reduce(Cat(_, _))
  )
  val mask_Efp_b_onehot = Cat(
    Efp_b === 0.U | Efp_b === 1.U,
    (for (i <- 2 until significandWidth + 1) yield
      (Efp_b === i.U).asUInt
      ).reduce(Cat(_, _))
  )
  val U_CS0 = Module(new ClosePathAdderF16Pipeline(adderWidth = significandWidth, adderType = "CS0"))
  U_CS0.io.adder_op0 := significand_fp_a
  U_CS0.io.adder_op1 := significand_fp_b
  val CS0 = U_CS0.io.result(significandWidth-1,0)
  val priority_lshift_0 = CS0 | mask_Efp_a_onehot

  val U_CS1 = Module(new ClosePathAdderF16Pipeline(adderWidth = significandWidth, adderType = "CS1"))
  U_CS1.io.adder_op0 := significand_fp_b
  U_CS1.io.adder_op1 := significand_fp_a
  val CS1 = U_CS1.io.result(significandWidth-1,0)
  val priority_lshift_1 = CS1 | mask_Efp_b_onehot

  val U_CS2 = Module(new ClosePathAdderF16Pipeline(adderWidth = significandWidth, adderType = "CS2"))
  U_CS2.io.adder_op0 := Cat(1.U,fp_a_mantissa)
  U_CS2.io.adder_op1 := Cat(1.U,fp_b_mantissa)
  val CS2 = U_CS2.io.result(significandWidth,0)
  val priority_lshift_2 = CS2 | Cat(mask_Efp_a_onehot,Efp_a===(significandWidth+1).U)

  val U_CS3 = Module(new ClosePathAdderF16Pipeline(adderWidth = significandWidth, adderType = "CS3"))
  U_CS3.io.adder_op0 := Cat(1.U,fp_b_mantissa)
  U_CS3.io.adder_op1 := Cat(1.U,fp_a_mantissa)
  val CS3 = U_CS3.io.result(significandWidth,0)
  val priority_lshift_3 = CS3 | Cat(mask_Efp_b_onehot,Efp_b===(significandWidth+1).U)

  val U_CS4 = Module(new ClosePathAdderF16Pipeline(adderWidth = significandWidth, adderType = "CS0"))
  U_CS4.io.adder_op0 := RegNext(Mux(Efp_b_is_greater,significand_fp_b,significand_fp_a))
  U_CS4.io.adder_op1 := RegNext(Mux(Efp_b_is_greater,Cat(0.U,significand_fp_a(significandWidth-1,1)),Cat(0.U,significand_fp_b(significandWidth-1,1))))
  val CS4_reg = U_CS4.io.result

  val close_sign_result = Wire(Bool())
  val close_exponent_result = Wire(UInt(exponentWidth.W))
  val close_fraction_result = Wire(UInt((significandWidth - 1).W))
  val CS2_round_up = significand_fp_b(0) & (
    (RUP & !fp_a_sign) | (RDN & fp_a_sign) | (RNE & CS2(1) & CS2(0)) | RMM
    )
  val CS3_round_up = significand_fp_a(0) & (
    (RUP & fp_a_sign) | (RDN & !fp_a_sign) | (RNE & CS3(1) & CS3(0)) | RMM
    )
  val sel_CS0 = exp_is_equal & !U_CS0.io.result.head(1).asBool
  val sel_CS1 = exp_is_equal & U_CS0.io.result.head(1).asBool
  val sel_CS2 = !exp_is_equal & !Efp_b_is_greater & ((!U_CS2.io.result.head(1).asBool | !U_CS2.io.result(0).asBool) | !CS2_round_up)
  val sel_CS3 = !exp_is_equal &  Efp_b_is_greater & ((!U_CS3.io.result.head(1).asBool | !U_CS3.io.result(0).asBool) | !CS3_round_up)
  val sel_CS4 = !exp_is_equal & (
    (!Efp_b_is_greater & U_CS2.io.result.head(1).asBool & U_CS2.io.result(0).asBool & CS2_round_up) |
      (Efp_b_is_greater & U_CS3.io.result.head(1).asBool & U_CS3.io.result(0).asBool & CS3_round_up)
    )
  val CS_0123_result = Mux1H(
    Seq(
      sel_CS0,
      sel_CS1,
      sel_CS2,
      sel_CS3
    ),
    Seq(
      Cat(CS0,0.U),
      Cat(CS1,0.U),
      CS2,
      CS3
    )
  )
  val mask_onehot = Mux1H(
    Seq(
      sel_CS0,
      sel_CS1,
      sel_CS2,
      sel_CS3
    ),
    Seq(
      Cat(mask_Efp_a_onehot,1.U),
      Cat(mask_Efp_b_onehot,1.U),
      Cat(mask_Efp_a_onehot,Efp_a===(significandWidth+1).U),
      Cat(mask_Efp_b_onehot,Efp_b===(significandWidth+1).U)
    )
  )
  val priority_mask = CS_0123_result | mask_onehot





  val lzd_0123 = LZD(priority_mask)
  val U_Lshift = Module(new CloseShiftLeftWithMux(CS_0123_result.getWidth,priority_mask.getWidth.U.getWidth))
  U_Lshift.io.src := RegNext(CS_0123_result)
  U_Lshift.io.shiftValue := RegNext(lzd_0123)
  val CS_0123_lshift_result_reg = U_Lshift.io.result(significandWidth,1)
  NX := RegNext(Mux(
    (sel_CS2 & (U_CS2.io.result.head(1).asBool & U_CS2.io.result(0).asBool & !CS2_round_up)) |
      (sel_CS3 & (U_CS3.io.result.head(1).asBool & U_CS3.io.result(0).asBool & !CS3_round_up)) | sel_CS4,
    B_guard,
    0.U
  ))
  close_fraction_result := Mux(RegNext(sel_CS4),CS4_reg.tail(1),CS_0123_lshift_result_reg.tail(1))

  val lshift_result_head_is_one = ((RegNext(EA-1.U) > RegNext(lzd_0123)) & RegNext(CS_0123_result).orR) | RegNext(mask_onehot & CS_0123_result).orR
  val EA_sub_value = Mux(
    RegNext(sel_CS4),
    0.U(exponentWidth.W),
    Mux(
      lshift_result_head_is_one,
      RegNext(lzd_0123).asTypeOf(EA),
      RegNext(EA)
    )
  )
  close_exponent_result := RegNext(EA) - EA_sub_value
  close_sign_result := RegNext(Mux1H(
    Seq(
      sel_CS0 & (exp_is_equal & (U_CS0.io.result.head(1).asBool | U_CS1.io.result.head(1).asBool)),
      sel_CS0 & exp_is_equal & (!U_CS0.io.result.head(1).asBool & !U_CS1.io.result.head(1).asBool),
      sel_CS1,
      sel_CS2,
      sel_CS3,
      sel_CS4
    ),
    Seq(
      fp_a_sign,
      RDN,
      !fp_a_sign,
      fp_a_sign,
      !fp_a_sign,
      Mux(Efp_b_is_greater,!fp_a_sign,fp_a_sign)
    )
  ))
  io.fp_c := Cat(close_sign_result,close_exponent_result,close_fraction_result)
  if (hasMinMaxCompare) io.CS1 := U_CS1.io.result
  if (is_print){
    printf(p"*****close path*****\n")
    printf(p"CS1 = ${Binary(CS1)}\n")
    printf(p"CS0 = ${Binary(CS0)}\n")
  }

}

private[vector] class FloatAdderF16Pipeline(val is_print:Boolean = false,val hasMinMaxCompare:Boolean = false) extends Module {

  val exponentWidth = 5
  val significandWidth = 11
  val floatWidth = exponentWidth + significandWidth
  val io = IO(new Bundle() {
    val fp_a, fp_b  = Input (UInt(floatWidth.W))
    val fp_c        = Output(UInt(floatWidth.W))
    val is_sub      = Input (Bool())
    val round_mode  = Input (UInt(3.W))
    val fflags      = Output(UInt(5.W))
    val op_code     = if (hasMinMaxCompare) Input(UInt(4.W)) else Input(UInt(0.W))
  })
  val EOP = (io.fp_a.head(1) ^ io.is_sub ^ io.fp_b.head(1)).asBool
  val U_far_path = Module(new FarPathF16Pipeline(exponentWidth = exponentWidth,significandWidth = significandWidth, is_print = is_print, hasMinMaxCompare=hasMinMaxCompare))
  U_far_path.io.fp_a := io.fp_a
  U_far_path.io.fp_b := io.fp_b
  U_far_path.io.is_sub := io.is_sub
  U_far_path.io.round_mode := io.round_mode
  val U_close_path = Module(new ClosePathF16Pipeline(exponentWidth = exponentWidth,significandWidth = significandWidth, is_print = is_print, hasMinMaxCompare=hasMinMaxCompare))
  U_close_path.io.fp_a := io.fp_a
  U_close_path.io.fp_b := io.fp_b
  U_close_path.io.round_mode := io.round_mode
  val Efp_a = io.fp_a(floatWidth-2, floatWidth-1-exponentWidth)
  val Efp_b = io.fp_b(floatWidth-2, floatWidth-1-exponentWidth)
  val Efp_a_is_not_zero  = Efp_a.orR
  val Efp_b_is_not_zero  = Efp_b.orR
  val Efp_a_is_all_one   = Efp_a.andR
  val Efp_b_is_all_one   = Efp_b.andR
  val absEaSubEb = U_far_path.io.absEaSubEb
  val is_far_path     = !EOP | absEaSubEb(absEaSubEb.getWidth - 1, 1).orR | (absEaSubEb === 1.U & (Efp_a_is_not_zero ^ Efp_b_is_not_zero))
  val is_close_path   =  EOP & (!absEaSubEb(absEaSubEb.getWidth - 1, 1).orR)
  val fp_a_mantissa            = io.fp_a.tail(1 + exponentWidth)
  val fp_b_mantissa            = io.fp_b.tail(1 + exponentWidth)
  val fp_a_mantissa_isnot_zero = io.fp_a.tail(1 + exponentWidth).orR
  val fp_b_mantissa_isnot_zero = io.fp_b.tail(1 + exponentWidth).orR
  val fp_a_is_NAN        = Efp_a_is_all_one & fp_a_mantissa_isnot_zero
  val fp_a_is_SNAN       = Efp_a_is_all_one & fp_a_mantissa_isnot_zero & !io.fp_a(significandWidth-2)
  val fp_b_is_NAN        = Efp_b_is_all_one & fp_b_mantissa_isnot_zero
  val fp_b_is_SNAN       = Efp_b_is_all_one & fp_b_mantissa_isnot_zero & !io.fp_b(significandWidth-2)
  val fp_a_is_infinite   = Efp_a_is_all_one & (!fp_a_mantissa_isnot_zero)
  val fp_b_is_infinite   = Efp_b_is_all_one & (!fp_b_mantissa_isnot_zero)
  val float_adder_fflags = Wire(UInt(5.W))
  val float_adder_result = Wire(UInt(floatWidth.W))
  when(RegNext((fp_a_is_SNAN | fp_b_is_SNAN) | (EOP & fp_a_is_infinite & fp_b_is_infinite))){
    float_adder_fflags := "b10000".U
  }.elsewhen(RegNext(fp_a_is_NAN | fp_b_is_NAN | fp_a_is_infinite | fp_b_is_infinite)){
    float_adder_fflags := "b00000".U
  }.otherwise{
    float_adder_fflags := Mux(RegNext(is_far_path),U_far_path.io.fflags,U_close_path.io.fflags)
  }
  when(RegNext(fp_a_is_NAN | fp_b_is_NAN | (EOP & fp_a_is_infinite & fp_b_is_infinite))){

    float_adder_result := Cat(0.U,Fill(exponentWidth,1.U),1.U,Fill(significandWidth-2,0.U))
  }.elsewhen(RegNext(fp_a_is_infinite | fp_b_is_infinite)) {
    float_adder_result := Cat(RegNext(Mux(fp_a_is_infinite,io.fp_a.head(1),io.is_sub^io.fp_b.head(1))),Fill(exponentWidth,1.U),Fill(significandWidth-1,0.U))
  }.otherwise{
    float_adder_result := Mux(RegNext(is_far_path),U_far_path.io.fp_c,U_close_path.io.fp_c)
  }
  if (hasMinMaxCompare) {
    val is_add = io.op_code === 0.U
    val is_min = io.op_code === 1.U
    val is_max = io.op_code === 2.U
    val is_feq = io.op_code === 3.U
    val is_fne = io.op_code === 4.U
    val is_flt = io.op_code === 5.U
    val is_fle = io.op_code === 6.U
    val is_fgt = io.op_code === 7.U
    val is_fge = io.op_code === 8.U
    val fp_a_sign = io.fp_a.head(1)
    val fp_b_sign = io.fp_b.head(1)
    val fp_b_sign_is_greater = fp_a_sign & !fp_b_sign
    val fp_b_exponent_is_greater = U_far_path.io.isEfp_bGreater
    val fp_b_significand_is_greater = !U_close_path.io.CS1.head(1) & (fp_a_mantissa =/= fp_b_mantissa)
    val fp_b_is_greater = fp_b_sign_is_greater |
      !fp_b_sign_is_greater & fp_b_exponent_is_greater |
      !fp_b_sign_is_greater & !fp_b_exponent_is_greater & fp_b_significand_is_greater
    val fp_b_is_equal = io.fp_a === io.fp_b
    val fp_b_is_less = !fp_b_is_greater & !fp_b_is_equal
    val result_min = Wire(UInt(floatWidth.W))
    val result_max = Wire(UInt(floatWidth.W))
    val result_feq = Wire(UInt(floatWidth.W))
    val result_fne = Wire(UInt(floatWidth.W))
    val result_flt = Wire(UInt(floatWidth.W))
    val result_fle = Wire(UInt(floatWidth.W))
    val result_fgt = Wire(UInt(floatWidth.W))
    val result_fge = Wire(UInt(floatWidth.W))
    val out_NAN = Cat(0.U,Fill(exponentWidth,1.U),1.U,Fill(significandWidth-2,0.U))
    result_min := Mux1H(
      Seq(
        !fp_a_is_NAN & !fp_b_is_NAN,
        !fp_a_is_NAN &  fp_b_is_NAN,
        fp_a_is_NAN & !fp_b_is_NAN,
        fp_a_is_NAN &  fp_b_is_NAN,
      ),
      Seq(
        Mux(fp_b_is_less,io.fp_b,io.fp_a),
        io.fp_a,
        io.fp_b,
        out_NAN
      )
    )
    result_max := Mux1H(
      Seq(
        !fp_a_is_NAN & !fp_b_is_NAN,
        !fp_a_is_NAN &  fp_b_is_NAN,
        fp_a_is_NAN & !fp_b_is_NAN,
        fp_a_is_NAN &  fp_b_is_NAN,
      ),
      Seq(
        Mux(fp_b_is_greater.asBool,io.fp_b,io.fp_a),
        io.fp_a,
        io.fp_b,
        out_NAN
      )
    )
    result_feq := Mux(fp_a_is_NAN | fp_b_is_NAN,0.U,fp_b_is_equal)
    result_fne := !result_feq
    result_flt := Mux(fp_a_is_NAN | fp_b_is_NAN,0.U,fp_b_is_greater)
    result_fle := Mux(fp_a_is_NAN | fp_b_is_NAN,0.U,fp_b_is_greater | fp_b_is_equal)
    result_fgt := Mux(fp_a_is_NAN | fp_b_is_NAN,0.U,fp_b_is_less)
    result_fge := Mux(fp_a_is_NAN | fp_b_is_NAN,0.U,fp_b_is_less | fp_b_is_equal)
    val result_stage0 = Mux1H(
      Seq(
        is_min,
        is_max,
        is_feq,
        is_fne,
        is_flt,
        is_fle,
        is_fgt,
        is_fge
      ),
      Seq(
        result_min,
        result_max,
        result_feq,
        result_fne,
        result_flt,
        result_fle,
        result_fgt,
        result_fge
      )
    )
    val fflags_NV_stage0 = ((is_min | is_max) & (fp_a_is_SNAN | fp_b_is_SNAN)) |
      ((is_feq | is_fne) & (fp_a_is_SNAN | fp_b_is_SNAN)) |
      ((is_flt | is_fle | is_fgt | is_fge) & (fp_a_is_NAN | fp_b_is_NAN))
    val fflags_stage0 = Cat(fflags_NV_stage0,0.U(4.W))
    io.fp_c := Mux(RegNext(is_add),float_adder_result,RegNext(result_stage0))
    io.fflags := Mux(RegNext(is_add),float_adder_fflags,RegNext(fflags_stage0))
  }
  else {
    io.fp_c := float_adder_result
    io.fflags := float_adder_fflags
  }
}