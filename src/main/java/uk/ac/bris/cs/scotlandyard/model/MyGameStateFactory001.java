package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory001 implements Factory<GameState> {
	private void testDetectiveLocation( List<Player> detectives){

		for(int j = 0; j < detectives.size() - 1 ; j++){if(detectives.get(j).location() == detectives.get(j+1).location())
			throw new IllegalArgumentException("2 Pieces on the same location!");
		}

	}
	private ImmutableList<Player> initEveryone(List<Player> detectives, Player mrX){
		List<Player> list = new ArrayList<>();
		list.add(mrX);
		list.addAll(detectives);
		return ImmutableList.copyOf(list);

	}
	    final private class MyGameState implements GameState {
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;
		private ImmutableList<Player> everyone;

////////////////////////////////////////////////////


		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives){
			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
			this.everyone=initEveryone(detectives,mrX);
////////////////////////////////////////////gametest
			if(setup.graph.hashCode() == 0)
				throw new IllegalArgumentException("Graph empty");
			if(remaining.isEmpty()) throw new IllegalArgumentException("No players!");
			if ( setup .moves .isEmpty ( ) ) throw new  IllegalArgumentException ( "Moves 是空的！" )  ;
			if ( detectives .isEmpty ()) throw new  IllegalArgumentException  ( "Detectives is empty!" );
			if (mrX.isDetective()){throw new  IllegalArgumentException("no mrx");
			}
////////////////////////////////////////////////// detective
			for (Player p:detectives){
				if(!p.isDetective()){
					throw new IllegalArgumentException("There cannot be more than 1 MrX!");}
				if (p.has(ScotlandYard.Ticket.DOUBLE))
					throw new IllegalArgumentException("detectives cant have double");
				if (p.has(ScotlandYard.Ticket.SECRET))
					throw new IllegalArgumentException("detectives shouldn't have secret ticket");
			}
///////////////////////////////////////////////////loction
			testDetectiveLocation(detectives);
			//////////////////////////////////////winner
			/*Set<Piece> win = new HashSet<>();
			if(detectivesWin()){
				for(Player x : detectives)
					win.add(x.piece());
			}
			if(mrxWins())
				win.add(mrX.piece());*/
			this.winner = getWinner();
		}
///////////////////////////////////////
		@Nonnull
		@Override
		public GameSetup getSetup() {
			return this.setup;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getPlayers() {
			//return a set to conatain mrx and palyers
			Set<Piece> p = new HashSet<>();
			p.add(mrX.piece());
			for(Player d : this.detectives)
				p.add(d.piece());
			return ImmutableSet.copyOf(p);
		}

		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			for(Player p : this.detectives)
				if(p.piece() == detective){
					return Optional.of(p.location());}
			return Optional.empty();
		}

		@Nonnull
		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			for (Player p:everyone){
				if (p.piece()==piece){
					return Optional.of(new TicketBoard() {
						@Override
						public int getCount(@Nonnull ScotlandYard.Ticket ticket) {
							return p.tickets().get(ticket);
						}
					});
				}
			}
			return Optional.empty();
		}

		@Nonnull
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return log;
		}
//singlr move
private static ImmutableSet<Move.SingleMove> makeSingleMoves(
		GameSetup setup,
		List<Player> detectives,
		Player player,
		int source){
	final var singleMoves = new ArrayList<Move.SingleMove>();

	for(int destination : setup.graph.adjacentNodes(source)) {
		boolean locationEmpty = true;

		for(Player p : detectives) {
			if (p.location() == destination)
				locationEmpty = false;
		}
		if(locationEmpty){
			for (ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {
				if (player.has(t.requiredTicket()))
					singleMoves.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));
				if(player.isMrX() && player.has(ScotlandYard.Ticket.SECRET))
					singleMoves.add(new Move.SingleMove(player.piece(), source, ScotlandYard.Ticket.SECRET, destination));
			}
		}
	}
	return ImmutableSet.copyOf(singleMoves);
}


		private static ImmutableSet<Move.DoubleMove> makeDoubleMoves(
				GameSetup setup,
				List<Player> detectives,
				Player mrX,
				int source
		){
			final var doubleMoves = new ArrayList<Move.DoubleMove>();
			Set<Move.SingleMove> singleMoves = new HashSet<>(makeSingleMoves(setup, detectives, mrX, source));
			for(Move.SingleMove move1  : singleMoves){
				int destination1 = move1.destination;
				Player tmp = mrX.use(move1.tickets());
				for(int destination2 : setup.graph.adjacentNodes(destination1)){
					boolean locationEmpty = true;
					for(Player p : detectives) {
						if (p.location() == destination2) {
							locationEmpty = false;
						}
					}
					if(locationEmpty){
						for (ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(destination1, destination2, ImmutableSet.of())) {
							if (tmp.has(t.requiredTicket()))
								doubleMoves.add(new Move.DoubleMove(mrX.piece(),source,move1.ticket,destination1,t.requiredTicket(),destination2));
							if(mrX.has(ScotlandYard.Ticket.SECRET))
								doubleMoves.add(new Move.DoubleMove(mrX.piece(),source,move1.ticket,destination1, ScotlandYard.Ticket.SECRET,destination2));

						}
					}

				}
			}
			return ImmutableSet.copyOf(doubleMoves);
		}
		private Player getPlayer(Piece piece){
			Player tmp=null;
			for(Player p : everyone)
				if(p.piece().equals(piece))
					tmp = p;
			return tmp;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() {
			return this.winner;
		}


		@Nonnull
		@Override

		public ImmutableSet<Move> getAvailableMoves() {
			if(!getWinner().isEmpty())
				return ImmutableSet.copyOf(new HashSet<>());

			Set<Move.SingleMove> singleMoves = new HashSet<>();
			//Player player;
			for (Player player : everyone) {
				if (remaining.contains(player.piece())) {
					singleMoves.addAll(makeSingleMoves(setup, detectives, player, player.location()));
				}
			}

			Set<Move> moves = new HashSet<>(singleMoves);
			//Checking that mrX can use the DOUBLE ticket, or if he has one
			if(this.remaining.contains(mrX.piece()) && mrX.has(ScotlandYard.Ticket.DOUBLE)&&this.log.size() +2 <=setup.moves.size()){
				Set<Move.DoubleMove> doubleMoves = new HashSet<>(makeDoubleMoves(setup, detectives, mrX, mrX.location()));
				moves.addAll(doubleMoves);
			}
			this.moves = ImmutableSet.copyOf(moves);
			return this.moves;
		}

			private int visitMe(Move move){

				return move.accept(new Move.Visitor<>(){

					@Override
					public Integer visit(Move.SingleMove move1) {
						return move1.destination;
					}

					@Override
					public Integer visit(Move.DoubleMove move1) {
						return move1.destination2;
					}
				});
			}

		private List<LogEntry> updateLog(Move move,int location){
			List<LogEntry> newLog = new ArrayList<>(this.log);
			for(ScotlandYard.Ticket t : move.tickets()){
				int size = newLog.size();
				if(t != ScotlandYard.Ticket.DOUBLE){
					if(setup.moves.get(size) )
						newLog.add(LogEntry.reveal(t,location));
					else newLog.add(LogEntry.hidden(t));}
			}
			return newLog;
		}

		private Set<Piece> transformSet(List<Player> everyone){
			Set<Piece> pieces = new HashSet<>();
			for(Player x : everyone)
				pieces.add(x.piece());

			return pieces;
		}
		@Nonnull
		@Override
		public GameState advance(Move move) {
			getAvailableMoves();
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);
//创建log和detectiv
			List<LogEntry> newLogEntry = new ArrayList<>(this.log);
			List<Player> detectiveList = new ArrayList<>(); //list with all detectives; at first it also contains mrX to ease the work
			Set<Piece> remainingAfter =new HashSet<>(this.remaining);

			for(Player p : everyone){

				Player tmp = p;
				if(p.piece() == move.commencedBy()){

					tmp=tmp.use(move.tickets());

					int location = visitMe(move);
					tmp=tmp.at(location);

					if(move.commencedBy().isMrX())
						newLogEntry = updateLog(move,location);
				}
				detectiveList.add(tmp);
			}

			for(Player p : detectives)
				if(makeSingleMoves(setup,detectives,p,p.location()).isEmpty())
					remainingAfter.remove(p.piece());
					//removePiece(remainingAfter,p.piece());
//检查然后初始化
			if(remainingAfter.size() == 1 ){
				if (remainingAfter.contains(this.mrX.piece())){
					remainingAfter = transformSet(detectiveList);
					remainingAfter.remove(move.commencedBy());
					//removePiece(remainingAfter,move.commencedBy());
				}
				else{
					remainingAfter = transformSet(detectiveList);
					for(Player p : detectives){
						remainingAfter.remove(p.piece());
					}
						//removePiece(remainingAfter,p.piece());
				}
			}
			else remainingAfter.remove(move.commencedBy());

				//removePiece(remainingAfter, move.commencedBy());

			Player mrx = detectiveList.get(0);

			if(move.commencedBy().isDetective()){

				for(ScotlandYard.Ticket t : move.tickets())
					mrx = mrx.give(t);

			}
			detectiveList.remove(0);
			return new MyGameState(setup,ImmutableSet.copyOf(remainingAfter),ImmutableList.copyOf(newLogEntry),mrx,ImmutableList.copyOf(detectiveList));

		}

		private boolean detectivesWin(){
			boolean detectivesWin = false;
			for(Player p : detectives)
				if(p.location() == mrX.location())
					detectivesWin = true;
			if(makeSingleMoves(setup,detectives,mrX, mrX.location()).isEmpty() && this.remaining.contains(mrX.piece()))
				detectivesWin = true;
			return detectivesWin;

		}
		private ImmutableSet winerget(){

			Set<Piece> winers = new HashSet<>();

			if(this.log.size() == setup.moves.size() && this.remaining.contains(mrX.piece())){
				winers.add(mrX.piece());}
			int cantmovedetect = 0;
			for(Player p : detectives){
				if(makeSingleMoves(setup,detectives,p, p.location()).isEmpty())
					cantmovedetect ++;
			}
			if(cantmovedetect== detectives.size()){
				winers.add(mrX.piece());
			}

//----------------------------------------
			if(detectivesWin()){
				for(Player x : detectives)
					winers.add(x.piece());
			}
			return ImmutableSet.copyOf(winers);
		}


	}
	//--------------------------------------
	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		// TODO
		//throw new RuntimeException("Implement me!");
		MyGameState S=new MyGameState(setup, ImmutableSet.of(Piece.MrX.MRX), ImmutableList.of(), mrX, detectives);
		return S;

	}

}
