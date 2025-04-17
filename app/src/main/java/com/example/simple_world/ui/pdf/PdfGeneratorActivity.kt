package com.example.simple_world.ui.pdf
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.marginEnd
import androidx.drawerlayout.widget.DrawerLayout
import com.example.simple_world.R
import com.example.simple_world.databinding.ActivityPdfGeneratorBinding
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import java.io.File
import java.io.FileOutputStream

class PdfGeneratorActivity : AppCompatActivity() {

    val pageEditTexts = mutableListOf<EditText>()
    private var currentFocusedEditText: EditText? = null

    private lateinit var binding: ActivityPdfGeneratorBinding
    val maxColors = 5
    private lateinit var addColorView:View
    private var selectedIndicator: View? = null

    private var currentTextColor: Int = Color.BLACK
    private var currentTextSizeSp: Float = 16f // default
    private var selectedImageView: ImageView? = null
    private var selectedImageUri: Uri? = null
    private var selectedImageSpan: ImageSpan? = null


    private lateinit var spannableBuilder: SpannableStringBuilder

    var isTextBold = false
    var isTextItalic = false
    var isTextUnderline = false
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val imageView = ImageView(this).apply {
                layoutParams = ViewGroup.LayoutParams(200, 200)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(uri)
                setPadding(8, 8, 8, 8)
                setBackgroundColor(Color.TRANSPARENT)

                setOnClickListener {
                    selectedImageView?.setBackgroundColor(Color.TRANSPARENT)
                    selectedImageView = this
                    selectedImageUri = uri
                    setBackgroundColor(Color.BLUE) // Highlight selected
                }
            }
            binding.drawerLayoutContainer.imageContainer.addView(imageView)

            //insertImageIntoEditText(it)
        }
    }



    // Holds color views (excluding the add button)
    val customColorViews = mutableListOf<View>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Setup binding
        binding = ActivityPdfGeneratorBinding.inflate(layoutInflater)

        setContentView(binding.root)

        supportActionBar?.hide()

        val drawerLayout = binding.drawerLayout
        val menuIcon = binding.menuIcon

        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {
                //menuIcon.visibility = View.GONE
            }

            override fun onDrawerClosed(drawerView: View) {
                //menuIcon.visibility = View.VISIBLE
            }

            override fun onDrawerStateChanged(newState: Int) {}
        })

        Handler(Looper.getMainLooper()).postDelayed({
            drawerLayout.openDrawer(GravityCompat.START)
        }, 500)
        addColorView=binding.drawerLayoutContainer.containerTextController.addColorView
        // Now access included layout via binding
        //val container = binding.pdfContentContainer.pdfPreviewContainer

        val (targetWidth, targetHeight) = getPdfContainerSize()
//        container.layoutParams = container.layoutParams?.apply {
//            width = targetWidth
//            height = targetHeight
//        }
        addNewPage()
        binding.drawerLayoutContainer.cropImageButton.setOnClickListener {
            selectedImageUri?.let { uri ->
                val cropFragment = CropImageFragment(uri) { croppedUri ->
                    // Replace image in grid and update URI
                    selectedImageView?.setImageURI(croppedUri)
                    selectedImageUri = croppedUri
                }
                cropFragment.show(supportFragmentManager, "CropImageFragment")
            } ?: Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
        }

        //setupImageSelectionHandler()
//        binding.pdfContentContainer.pdfTextInput.setOnFocusChangeListener { _, _ ->
//            showSliderIfImageSelected()
//        }





        initiateComponents()
        binding.drawerLayoutContainer.addImageButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
//TODO
        binding.generatePdfButton.setOnClickListener {
            generatePdfFromAllPages(binding.pageStackContainer,"testfile")
            //generatePdfFromView(binding.pdfContentContainer.pdfTextInput, "MyDiaryExport")
        }
        binding.drawerLayoutContainer.textControlToggle.setOnClickListener {
            if (binding.drawerLayoutContainer.containerTextController.textControlPanel.visibility == View.VISIBLE) {
                binding.drawerLayoutContainer.containerTextController.textControlPanel.visibility = View.GONE
            } else {
                binding.drawerLayoutContainer.containerTextController.textControlPanel.visibility = View.VISIBLE
            }
        }
        binding.drawerLayoutContainer.containerTextController.addColorView.setOnTouchListener(object : View.OnTouchListener {
            private var lastTapTime = 0L

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event?.action == MotionEvent.ACTION_DOWN) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTapTime < 300) {
                        openColorPicker()
                    }
                    lastTapTime = currentTime
                }
                return false
            }
        })
        setupColorView(binding.drawerLayoutContainer.containerTextController.colorWhite,binding.drawerLayoutContainer.containerTextController.indicatorWhite)
        setupColorView(binding.drawerLayoutContainer.containerTextController.colorBlack,binding.drawerLayoutContainer.containerTextController.indicatorBlack)

    }
    fun initiateComponents(){
        binding.drawerLayoutContainer.insertSelectedImageButton.setOnClickListener {
            selectedImageUri?.let { uri ->
                insertImageIntoEditText(uri)
            } ?: Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
        }
    }
    private fun addTextListener(editText: EditText){
        editText.apply {
            spannableBuilder = SpannableStringBuilder()
            setText(spannableBuilder, TextView.BufferType.SPANNABLE)

            addTextChangedListener(object : TextWatcher {
                private var lastStart = 0
                private var lastEnd = 0

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    lastStart = start
                    lastEnd = start + after
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val layout = editText.layout ?: return
                    val lineCount = layout.lineCount
                    val lastLineBottom = layout.getLineBottom(lineCount - 1)

//                    if (lastLineBottom >= editText.height - editText.paddingBottom) {
//                        // Add new page if not already the last EditText
//                        if (editText == pageEditTexts.last()) {
//                            addNewPage()
//                        }
//                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s != null && lastStart < s.length) {
                        val end = lastEnd.coerceAtMost(s.length)

                        // Check if an ImageSpan is in the current range
                        val hasImageSpan = s.getSpans(lastStart, end, ImageSpan::class.java).isNotEmpty()

                        if (!hasImageSpan) {
                            // Only apply text formatting if there's no image at the position

                            // Apply text color
                            s.setSpan(
                                ForegroundColorSpan(currentTextColor),
                                lastStart,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )

                            // Apply text size
                            s.setSpan(
                                AbsoluteSizeSpan(currentTextSizeSp.toInt(), true),
                                lastStart,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )

                            // Apply text style
                            if (isTextBold && isTextItalic) {
                                s.setSpan(StyleSpan(Typeface.BOLD_ITALIC), lastStart, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            } else if (isTextBold) {
                                s.setSpan(StyleSpan(Typeface.BOLD), lastStart, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            } else if (isTextItalic) {
                                s.setSpan(StyleSpan(Typeface.ITALIC), lastStart, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }

                            // Apply underline
                            if (isTextUnderline) {
                                s.setSpan(UnderlineSpan(), lastStart, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                        }
                    }
                }
            })
        }
    }
    private fun addNewPage(): View {
        val inflater = LayoutInflater.from(this)
        val newPage = inflater.inflate(R.layout.pdf_content_layout, binding.pageStackContainer, false)

        val editText = newPage.findViewById<EditText>(R.id.pdf_text_input)

        // 1. Add to list
        pageEditTexts.add(editText)

        // 2. Attach all your existing listeners
        setupAllListeners(editText)

        // 3. Add the view to container
        binding.pageStackContainer.addView(newPage)
        return newPage
    }

    private fun setupAllListeners(editText: EditText) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                currentFocusedEditText = editText
            }
        }

        showSliderIfImageSelected(editText)
        setupImageSelectionHandler(editText)
        addTextListener(editText)
        setupTextSizeSelecter(editText)
        setupFontStyleNormal(editText)
        setupOverflowWatcher(editText)

//        watchTextGrowth(editText)
//        setupHoldResize(editText)
//        setupFocusEvents(editText)
        // Any other custom behavior you already use
    }
    fun setupOverflowWatcher(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                editText.post {
                    val layout = editText.layout ?: return@post

                    // Calculate total rendered height (including images, large text, etc.)
                    val contentHeight = editText.layout?.height ?: 0
                    val visibleHeight = editText.height - editText.paddingTop - editText.paddingBottom

                    if (contentHeight >= visibleHeight) {
                        // Trigger overflow only when view height can't contain the content



                        // Text is overflowing in actual pixels

                        // 1. Find the overflow starting point
                        val overflowStart = layout.getLineStart(layout.lineCount - 1)
                        val overflowText = s?.subSequence(overflowStart, s.length).toString()

                        // 2. Remove overflow from current EditText
                        (s as? Editable)?.delete(overflowStart, s.length)


                        // 3. Add a new page and continue
                        val newPage = addNewPage()
                        val nextEditText = newPage.findViewById<EditText>(R.id.pdf_text_input)
                        nextEditText.setText(overflowText)
                        nextEditText.setSelection(nextEditText.text?.length ?: 0)
                        nextEditText.requestFocus()
                    }
                }
            }
        })

        // Just to ensure no scrollbars mess with layout
        editText.setOnTouchListener { _, _ ->
            editText.isVerticalScrollBarEnabled = false
            false
        }
    }




    fun showSliderIfImageSelected(editText: EditText) {

        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        val spannable = editText.text

        val imageSpans = spannable?.getSpans(selectionStart, selectionEnd, ImageSpan::class.java)

        if (imageSpans != null && imageSpans.isNotEmpty()) {
            val imageSpan = imageSpans[0]
            val drawable = imageSpan.drawable
            val currentWidth = drawable.intrinsicWidth
            val currentHeight = drawable.intrinsicHeight

            binding.imageResizeSlider.visibility = View.VISIBLE
            binding.imageResizeSlider.progress = 100 // assume current is 100%

            // Resize image dynamically
            binding.imageResizeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val scale = progress / 100f
                    val newWidth = (drawable.intrinsicWidth * scale).toInt()
                    val newHeight = (drawable.intrinsicHeight * scale).toInt()

                    drawable.setBounds(0, 0, newWidth, newHeight)

                    // Re-apply the span to update rendering
                    val start = spannable.getSpanStart(imageSpan)
                    val end = spannable.getSpanEnd(imageSpan)

                    if (start >= 0 && end >= 0) {
                        spannable.removeSpan(imageSpan)
                        spannable.setSpan(ImageSpan(drawable, ImageSpan.ALIGN_BASELINE), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        } else {
            binding.imageResizeSlider.visibility = View.GONE
        }
    }
    private fun setupImageSelectionHandler(editText: EditText) {
        //val editText = binding.pdfContentContainer.pdfTextInput

        editText.setOnLongClickListener {
            val selectionStart = editText.selectionStart
            val selectionEnd = editText.selectionEnd

            val spannable = editText.text as? Spannable ?: return@setOnLongClickListener false

            val imageSpans = spannable.getSpans(selectionStart, selectionEnd, ImageSpan::class.java)
            if (imageSpans.isNotEmpty()) {
                val imageSpan = imageSpans[0]
                selectedImageSpan = imageSpan

                // Extract drawable bounds (current size)
                val bounds = imageSpan.drawable.bounds
                val currentWidth = bounds.width()

                // Show slider & update it
                binding.imageResizeSlider.apply {
                    progress = currentWidth
                    visibility = View.VISIBLE

                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser && progress > 0 && selectedImageSpan != null) {
                                val drawable = selectedImageSpan!!.drawable

                                // Resize drawable
                                val newWidth = progress
                                val aspectRatio = drawable.intrinsicHeight.toFloat() / drawable.intrinsicWidth
                                val newHeight = (newWidth * aspectRatio).toInt()

                                drawable.setBounds(0, 0, newWidth, newHeight)

                                // Replace the span with updated size
                                val start = spannable.getSpanStart(selectedImageSpan)
                                val end = spannable.getSpanEnd(selectedImageSpan)

                                if (start >= 0 && end > start) {
                                    spannable.removeSpan(selectedImageSpan)
                                    val newSpan = ImageSpan(drawable, ImageSpan.ALIGN_BASELINE)
                                    spannable.setSpan(newSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    selectedImageSpan = newSpan
                                }
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    })
                }

                return@setOnLongClickListener true
            }

            false
        }
    }

private fun insertImageIntoEditText(imageUri: Uri) {
    println("trying image insertion $currentFocusedEditText")
    val editText = currentFocusedEditText ?: return  // Return if no field is focused

    val inputStream = contentResolver.openInputStream(imageUri)
    val drawable = Drawable.createFromStream(inputStream, imageUri.toString())
    inputStream?.close()

    drawable?.setBounds(0, 0, drawable.intrinsicWidth / 2, drawable.intrinsicHeight / 2)  // Resize if needed

    val span = ImageSpan(drawable!!, ImageSpan.ALIGN_BASELINE)
    val spannableString = SpannableString(" ")
    spannableString.setSpan(span, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    val start = editText.selectionStart
    editText.text?.insert(start, spannableString)
}


    fun setupColorView(colorView: View, indicatorView: View) {
        var lastTapTime = 0L

        colorView.setOnClickListener {
            val color = (colorView.background as ColorDrawable).color
            currentTextColor = color

            currentFocusedEditText?.applyColorToSelection(color)
            currentFocusedEditText?.setTextColor(currentTextColor)


            selectedIndicator?.visibility = View.GONE
            indicatorView.visibility = View.VISIBLE
            selectedIndicator = indicatorView
        }


        colorView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastTapTime < 300) {
                    openColorPicker(colorView)
                }
                lastTapTime = currentTime
            }
            false
        }
    }
    fun EditText.applyColorToSelection(color: Int) {
        val start = selectionStart
        val end = selectionEnd
        if (start >= 0 && end > start) {
            val spannable = text as Spannable
            spannable.setSpan(
                ForegroundColorSpan(color),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
    fun EditText.applyStyleToSelection() {
        val start = selectionStart
        val end = selectionEnd
        if (start >= 0 && end > start) {
            val spannable = text as Spannable

            if (isTextBold && isTextItalic) {
                spannable.setSpan(StyleSpan(Typeface.BOLD_ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else if (isTextBold) {
                spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else if (isTextItalic) {
                spannable.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            if (isTextUnderline) {
                spannable.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    fun setupFontStyleNormal(editText: EditText) {
        val underlineBold = binding.drawerLayoutContainer.underlineBold
        val underlineItalic = binding.drawerLayoutContainer.underlineItalic
        val underlineUnderline = binding.drawerLayoutContainer.underlineUnderline

        val iconBold = binding.drawerLayoutContainer.iconBold
        val iconItalic = binding.drawerLayoutContainer.iconItalic
        val iconUnderline = binding.drawerLayoutContainer.iconUnderline

        iconBold.setOnClickListener {
            isTextBold = !isTextBold
            underlineBold.visibility = if (isTextBold) View.VISIBLE else View.GONE
            editText.applyStyleToSelection()
        }

        iconItalic.setOnClickListener {
            isTextItalic = !isTextItalic
            underlineItalic.visibility = if (isTextItalic) View.VISIBLE else View.GONE
            editText.applyStyleToSelection()
        }

        iconUnderline.setOnClickListener {
            isTextUnderline = !isTextUnderline
            underlineUnderline.visibility = if (isTextUnderline) View.VISIBLE else View.GONE
            editText.applyStyleToSelection()
        }
    }




fun setupTextSizeSelecter(editText: EditText){
    val textSizeSeekBar = binding.drawerLayoutContainer.textSizeSeekBar
    val textSizeValue = binding.drawerLayoutContainer.textSizeValue

    textSizeSeekBar.max = 30
    textSizeSeekBar.progress = currentTextSizeSp.toInt()
    textSizeValue.text = "${currentTextSizeSp.toInt()}sp"

    textSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            val newSize = if (progress < 8) 8f else progress.toFloat() // Minimum 8sp
            currentTextSizeSp = newSize
            textSizeValue.text = "${newSize.toInt()}sp"

            // Optional: apply immediately to entire EditText
            editText.textSize = newSize
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    })
}


    fun openColorPicker(targetView: View? = null) {
        ColorPickerDialogBuilder
            .with(this)
            .setTitle("Pick a color")
            .initialColor(Color.BLACK)
            .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
            .density(10)
            .setPositiveButton("OK") { _, selectedColor, _ ->
                if (targetView == null) {
                    if (customColorViews.size >= (maxColors - 2)) return@setPositiveButton

                    // Create indicator view
                    val indicatorView = View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            32.dpToPx(),
                            4.dpToPx()
                        )
                        setBackgroundColor(Color.BLUE)
                        visibility = View.GONE
                    }

                    // Create the color view
                    val colorView = View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            32.dpToPx(),
                            32.dpToPx()
                        ).apply {
                            marginEnd = 8.dpToPx()
                        }
                        setBackgroundColor(selectedColor)

                        setOnClickListener {
                            currentTextColor=selectedColor
                            currentFocusedEditText?.applyColorToSelection(selectedColor)
                            currentFocusedEditText?.setTextColor(currentTextColor)
                            //yourEditText.setTextColor(selectedColor)

                            // Handle selection indicator
                            selectedIndicator?.visibility = View.GONE
                            indicatorView.visibility = View.VISIBLE
                            selectedIndicator = indicatorView
                        }

                        setOnTouchListener(object : View.OnTouchListener {
                            private var lastTapTime = 0L
                            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                                if (event?.action == MotionEvent.ACTION_DOWN) {
                                    val currentTime = System.currentTimeMillis()
                                    if (currentTime - lastTapTime < 300) {
                                        openColorPicker(this@apply)
                                    }
                                    lastTapTime = currentTime
                                }
                                return false
                            }
                        })
                    }

                    // Wrap in vertical layout
                    val container = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            marginEnd = 8.dpToPx()
                        }
                        addView(colorView)
                        addView(indicatorView)
                    }

                    customColorViews.add(colorView)

                    // Insert before addColorView
                    val index = binding.drawerLayoutContainer.containerTextController.colorPicker.indexOfChild(addColorView)
                    binding.drawerLayoutContainer.containerTextController.colorPicker.addView(container, index)

                    if (customColorViews.size >= (maxColors - 2)) {
                        addColorView.visibility = View.GONE
                    }
                } else {
                    targetView.setBackgroundColor(selectedColor)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .build()
            .show()
    }

    fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }

    private fun generatePdfFromAllPages(container: LinearLayout, fileName: String) {
        val pdfDocument = PdfDocument()

        for (i in 0 until container.childCount) {
            val pageView = container.getChildAt(i)

            // Measure and layout if needed (important if views weren't drawn yet)
            pageView.measure(
                View.MeasureSpec.makeMeasureSpec(pageView.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(pageView.height, View.MeasureSpec.EXACTLY)
            )
            //pageView.layout(0, 0, pageView.measuredWidth, pageView.measuredHeight)

            val bitmap = Bitmap.createBitmap(pageView.width, pageView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            pageView.draw(canvas)

            val pageInfo = PdfDocument.PageInfo.Builder(pageView.width, pageView.height, i + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)
        }

        val file = getPdfOutputFile(fileName)
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        Toast.makeText(this, "PDF saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }





    private fun getPdfOutputFile(fileName: String): File {
        val pdfDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "SimpleWorldPDFs"
        )
        if (!pdfDir.exists()) {
            pdfDir.mkdirs()
        }
        return File(pdfDir, "$fileName.pdf")
    }

    private fun getPdfContainerSize(): Pair<Int, Int> {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // A4 aspect ratio = 1 : 1.41
        val a4AspectRatio = 1.0 / 1.41

        // Use the smaller dimension to fit within screen while keeping ratio
        var targetWidth = screenWidth
        var targetHeight = (targetWidth / a4AspectRatio).toInt()

        if (targetHeight > screenHeight) {
            targetHeight = screenHeight
            targetWidth = (targetHeight * a4AspectRatio).toInt()
        }

        return Pair(targetWidth, targetHeight)
    }

}
