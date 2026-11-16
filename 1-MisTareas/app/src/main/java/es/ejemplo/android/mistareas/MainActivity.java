package es.ejemplo.android.mistareas;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.ejemplo.android.mistareas.databinding.ActivityMainBinding;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TareaAdapter.OnTareaListener {

    private ActivityMainBinding binding;
    private final List<Tarea> tareas = new ArrayList<>();
    private TareaAdapter adapter;
    private boolean vistaCuadricula = false;
    private int posicionSeleccionada = RecyclerView.NO_POSITION;
    private int contador = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);

        cargarTareasDeEjemplo();
        adapter = new TareaAdapter(tareas, this);
        binding.rvTareas.setAdapter(adapter);
        aplicarLayoutManager();

        binding.fab.setOnClickListener(v -> anadirTarea());

        actualizarContador();
    }

    private void cargarTareasDeEjemplo() {
        tareas.add(new Tarea("Repasar layouts", "LinearLayout, ConstraintLayout y FrameLayout", Tarea.PRIORIDAD_MEDIA));
        tareas.add(new Tarea("Practicar RecyclerView", "Adapter + ViewHolder", Tarea.PRIORIDAD_ALTA));
        tareas.add(new Tarea("Probar los menús", "Opciones, contextual y popup", Tarea.PRIORIDAD_ALTA));
        tareas.add(new Tarea("Leer sobre Material Design", "CardView y FAB", Tarea.PRIORIDAD_BAJA));
        tareas.add(new Tarea("Entregar la práctica", "Subir el .zip a la plataforma", Tarea.PRIORIDAD_MEDIA));
        for (int i = 1; i <= 15; i++) {
            tareas.add(new Tarea("Tarea de prueba " + i, "Desplaza la lista hacia abajo", Tarea.PRIORIDAD_BAJA));
        }
    }

    private void aplicarLayoutManager() {
        if (vistaCuadricula) {
            binding.rvTareas.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            binding.rvTareas.setLayoutManager(new LinearLayoutManager(this));
        }
        invalidateOptionsMenu();
    }

    private void anadirTarea() {
        Tarea nueva = new Tarea(getString(R.string.nueva_tarea, contador++),
                getString(R.string.nueva_tarea_descripcion), Tarea.PRIORIDAD_MEDIA);
        tareas.add(nueva);
        adapter.notifyItemInserted(tareas.size() - 1);
        binding.rvTareas.scrollToPosition(tareas.size() - 1);

        actualizarContador();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_cambiar_vista);
        if (vistaCuadricula) {
            item.setIcon(R.drawable.ic_view_list);
            item.setTitle(R.string.ver_lista);
        } else {
            item.setIcon(R.drawable.ic_grid_view);
            item.setTitle(R.string.ver_cuadricula);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_cambiar_vista) {
            vistaCuadricula = !vistaCuadricula;
            aplicarLayoutManager();
            return true;
        } else if (id == R.id.action_borrar_completadas) {
            borrarCompletadas();
            return true;
        } else if (id == R.id.action_ordenar) {
            // Ordena las tareas de mayor prioridad a menor
            tareas.sort((t1, t2) -> Integer.compare(t2.getPrioridad(), t1.getPrioridad()));
            adapter.notifyDataSetChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void borrarCompletadas() {
        int borradas = 0;
        for (int i = tareas.size() - 1; i >= 0; i--) {
            if (tareas.get(i).isCompletada()) {
                tareas.remove(i);
                adapter.notifyItemRemoved(i);
                borradas++;

            }
        }
        Toast.makeText(this, getString(R.string.tareas_borradas, borradas), Toast.LENGTH_SHORT).show();

        actualizarContador();
    }

    @Override
    public void onTareaPulsada(int posicion) {
        Tarea t = tareas.get(posicion);
        t.setCompletada(!t.isCompletada());
        adapter.notifyItemChanged(posicion);

        actualizarContador();
    }

    @Override
    public void onMenuTarea(View ancla, int posicion) {
        PopupMenu popup = new PopupMenu(this, ancla);
        popup.getMenuInflater().inflate(R.menu.menu_popup_prioridad, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            Tarea t = tareas.get(posicion);
            if (id == R.id.prioridad_alta) {
                t.setPrioridad(Tarea.PRIORIDAD_ALTA);
            } else if (id == R.id.prioridad_media) {
                t.setPrioridad(Tarea.PRIORIDAD_MEDIA);
            } else if (id == R.id.prioridad_baja) {
                t.setPrioridad(Tarea.PRIORIDAD_BAJA);
            } else {
                return false;
            }
            adapter.notifyItemChanged(posicion);
            return true;
        });
        popup.show();
    }

    @Override
    public void onCrearMenuContextual(ContextMenu menu, int posicion) {
        posicionSeleccionada = posicion;
        getMenuInflater().inflate(R.menu.menu_contextual_tarea, menu);
        menu.setHeaderTitle(tareas.get(posicion).getTitulo());
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int pos = posicionSeleccionada;
        if (pos == RecyclerView.NO_POSITION || pos >= tareas.size()) {
            return super.onContextItemSelected(item);
        }
        int id = item.getItemId();
        if (id == R.id.action_duplicar) {
            Tarea original = tareas.get(pos);
            Tarea copia = new Tarea(getString(R.string.copia_de, original.getTitulo()),
                    original.getDescripcion(), original.getPrioridad());
            tareas.add(pos + 1, copia);
            adapter.notifyItemInserted(pos + 1);
            return true;
        } else if (id == R.id.action_eliminar) {
            tareas.remove(pos);
            adapter.notifyItemRemoved(pos);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void actualizarContador() {
        int pendientes = 0;
        for (Tarea t : tareas) {
            if (!t.isCompletada()) pendientes++;
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(getString(R.string.contador_pendientes, pendientes));
        }
    }
}