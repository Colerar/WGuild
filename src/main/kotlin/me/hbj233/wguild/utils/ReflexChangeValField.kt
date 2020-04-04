package me.hbj233.wguild.utils
/*
import java.lang.reflect.Field
import java.lang.reflect.Modifier

@Throws(Exception::class)
fun changeStaticFinal(field: Field, newValue: Any?) {
    field.isAccessible = true // 如果field为private,则需要使用该方法使其可被访问
    val modifiersField: Field = Field::class.java.getDeclaredField("modifiers")
    modifiersField.isAccessible = true
    // 把指定的field中的final修饰符去掉
    modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
    field.set(null, newValue) // 为指定field设置新值
}*/
